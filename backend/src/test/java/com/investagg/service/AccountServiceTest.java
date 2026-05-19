package com.investagg.service;

import com.investagg.client.BrokerClient;
import com.investagg.dto.request.ConnectAccountRequest;
import com.investagg.dto.response.AccountResponse;
import com.investagg.entity.Broker;
import com.investagg.entity.InvestmentAccount;
import com.investagg.entity.User;
import com.investagg.entity.enums.AccountStatus;
import com.investagg.exception.AppException;
import com.investagg.exception.ConflictException;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.BrokerRepository;
import com.investagg.repository.InvestmentAccountRepository;
import com.investagg.repository.UserRepository;
import com.investagg.security.AesEncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private InvestmentAccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerClient brokerClient;

    @Mock
    private AesEncryptionService aesService;

    @InjectMocks
    private AccountService accountService;

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("u@example.com");
        user.setPassword("hashed");
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    private Broker buildBroker() {
        Broker broker = new Broker();
        broker.setId(UUID.randomUUID());
        broker.setName("Tinkoff");
        broker.setApiBase("https://api.tinkoff.ru");
        broker.setActive(true);
        return broker;
    }

    @Test
    void connectBrokerAccount_success_returnsResponse() {
        UUID userId = UUID.randomUUID();
        User user = buildUser();
        user.setId(userId);
        Broker broker = buildBroker();
        ConnectAccountRequest req = new ConnectAccountRequest(broker.getId(), "ACC-001", "raw-token");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(brokerRepository.findById(broker.getId())).thenReturn(Optional.of(broker));
        when(accountRepository.existsByUserIdAndBrokerIdAndAccountNumberAndDeletedAtIsNull(
                userId, broker.getId(), "ACC-001")).thenReturn(false);
        when(brokerClient.validateToken(anyString(), eq("raw-token"))).thenReturn(true);
        when(aesService.encrypt("raw-token")).thenReturn("enc-token");

        InvestmentAccount saved = new InvestmentAccount();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setBroker(broker);
        saved.setAccountNumber("ACC-001");
        saved.setBrokerToken("enc-token");
        saved.setAccountStatus(AccountStatus.ACTIVE);
        saved.setCreatedAt(OffsetDateTime.now());
        when(accountRepository.save(any())).thenReturn(saved);

        AccountResponse response = accountService.connectBrokerAccount(userId, req);

        assertThat(response.brokerName()).isEqualTo("Tinkoff");
        assertThat(response.accountNumber()).isEqualTo("ACC-001");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void connectBrokerAccount_duplicate_throwsConflict() {
        UUID userId = UUID.randomUUID();
        Broker broker = buildBroker();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser()));
        when(brokerRepository.findById(broker.getId())).thenReturn(Optional.of(broker));
        when(accountRepository.existsByUserIdAndBrokerIdAndAccountNumberAndDeletedAtIsNull(
                any(), any(), anyString())).thenReturn(true);

        ConnectAccountRequest req = new ConnectAccountRequest(broker.getId(), "ACC-001", "token");

        assertThatThrownBy(() -> accountService.connectBrokerAccount(userId, req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void connectBrokerAccount_invalidToken_throwsBadRequest() {
        UUID userId = UUID.randomUUID();
        Broker broker = buildBroker();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser()));
        when(brokerRepository.findById(broker.getId())).thenReturn(Optional.of(broker));
        when(accountRepository.existsByUserIdAndBrokerIdAndAccountNumberAndDeletedAtIsNull(
                any(), any(), anyString())).thenReturn(false);
        when(brokerClient.validateToken(anyString(), anyString())).thenReturn(false);

        ConnectAccountRequest req = new ConnectAccountRequest(broker.getId(), "ACC-001", "bad-token");

        assertThatThrownBy(() -> accountService.connectBrokerAccount(userId, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid broker token");
    }

    @Test
    void connectBrokerAccount_brokerNotFound_throwsEntityNotFound() {
        UUID userId = UUID.randomUUID();
        UUID brokerId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser()));
        when(brokerRepository.findById(brokerId)).thenReturn(Optional.empty());

        ConnectAccountRequest req = new ConnectAccountRequest(brokerId, "ACC-001", "token");

        assertThatThrownBy(() -> accountService.connectBrokerAccount(userId, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void disconnectAccount_notOwner_throwsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.disconnectAccount(userId, accountId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void disconnectAccount_success_softDeletes() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        User user = buildUser();
        user.setId(userId);
        Broker broker = buildBroker();

        InvestmentAccount account = new InvestmentAccount();
        account.setId(accountId);
        account.setUser(user);
        account.setBroker(broker);
        account.setAccountNumber("ACC-001");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(OffsetDateTime.now());

        when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);

        accountService.disconnectAccount(userId, accountId);

        assertThat(account.getDeletedAt()).isNotNull();
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.REVOKED);
    }
}
