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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final InvestmentAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BrokerRepository brokerRepository;
    private final BrokerClient brokerClient;
    private final AesEncryptionService aesService;

    @Transactional
    public AccountResponse connectBrokerAccount(UUID userId, ConnectAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Broker broker = brokerRepository.findById(request.brokerId())
                .orElseThrow(() -> new EntityNotFoundException("Broker not found"));

        if (accountRepository.existsByUserIdAndBrokerIdAndAccountNumberAndDeletedAtIsNull(
                userId, broker.getId(), request.accountNumber())) {
            throw new ConflictException("Account already connected");
        }

        if (!brokerClient.validateToken(broker.getApiBase(), request.brokerToken())) {
            throw new AppException("Invalid broker token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN");
        }

        InvestmentAccount account = new InvestmentAccount();
        account.setUser(user);
        account.setBroker(broker);
        account.setAccountNumber(request.accountNumber());
        account.setBrokerToken(aesService.encrypt(request.brokerToken()));
        account.setAccountStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        return toResponse(account);
    }

    public List<AccountResponse> getAccounts(UUID userId) {
        return accountRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void validateOwnership(UUID userId, UUID accountId) {
        if (!accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId).isPresent()) {
            throw new ForbiddenException("Account not found or access denied");
        }
    }

    @Transactional
    public void disconnectAccount(UUID userId, UUID accountId) {
        InvestmentAccount account = accountRepository
                .findByIdAndUserIdAndDeletedAtIsNull(accountId, userId)
                .orElseThrow(() -> new ForbiddenException("Account not found or access denied"));

        account.setDeletedAt(OffsetDateTime.now());
        account.setAccountStatus(AccountStatus.REVOKED);
        accountRepository.save(account);
    }

    private AccountResponse toResponse(InvestmentAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getBroker().getId(),
                account.getBroker().getName(),
                account.getAccountNumber(),
                account.getAccountStatus(),
                account.getSyncedAt(),
                account.getCreatedAt()
        );
    }
}
