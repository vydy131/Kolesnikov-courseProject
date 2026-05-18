package com.investagg.service;

import com.investagg.client.BrokerClient;
import com.investagg.client.BrokerOrderRequest;
import com.investagg.dto.request.TradeOrderRequest;
import com.investagg.dto.response.TradeOrderResponse;
import com.investagg.entity.Broker;
import com.investagg.entity.InvestmentAccount;
import com.investagg.entity.TradeOrder;
import com.investagg.entity.User;
import com.investagg.entity.enums.AccountStatus;
import com.investagg.entity.enums.OrderDirection;
import com.investagg.entity.enums.OrderStatus;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.InvestmentAccountRepository;
import com.investagg.repository.TradeOrderRepository;
import com.investagg.repository.TransactionRepository;
import com.investagg.security.AesEncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private TradeOrderRepository orderRepository;

    @Mock
    private InvestmentAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BrokerClient brokerClient;

    @Mock
    private AesEncryptionService aesService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private InvestmentAccount buildAccount(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("u@example.com");

        Broker broker = new Broker();
        broker.setId(UUID.randomUUID());
        broker.setName("Tinkoff");
        broker.setApiBase("https://api.example.com");

        InvestmentAccount account = new InvestmentAccount();
        account.setId(UUID.randomUUID());
        account.setUser(user);
        account.setBroker(broker);
        account.setAccountNumber("ACC-001");
        account.setBrokerToken("enc-token");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(OffsetDateTime.now());
        return account;
    }

    @Test
    void createOrder_success_persistsOrderAndTransaction() {
        UUID userId = UUID.randomUUID();
        InvestmentAccount account = buildAccount(userId);

        TradeOrderRequest request = new TradeOrderRequest(
                account.getId(), "SBER", OrderDirection.BUY,
                new BigDecimal("10"), new BigDecimal("250.00")
        );

        when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(account.getId(), userId))
                .thenReturn(Optional.of(account));
        when(aesService.decrypt("enc-token")).thenReturn("plain-token");
        when(aesService.encrypt("plain-token")).thenReturn("enc-token");
        when(brokerClient.submitOrder(any(), any(BrokerOrderRequest.class))).thenReturn("broker-123");

        TradeOrder savedOrder = new TradeOrder();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setAccount(account);
        savedOrder.setTicker("SBER");
        savedOrder.setDirection(OrderDirection.BUY);
        savedOrder.setQty(new BigDecimal("10"));
        savedOrder.setPrice(new BigDecimal("250.00"));
        savedOrder.setOrderStatus(OrderStatus.PENDING);
        savedOrder.setBrokerOrderId("broker-123");
        savedOrder.setPlacedAt(OffsetDateTime.now());
        when(orderRepository.save(any())).thenReturn(savedOrder);

        TradeOrderResponse response = orderService.createOrder(userId, request);

        assertThat(response.ticker()).isEqualTo("SBER");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.brokerOrderId()).isEqualTo("broker-123");
        verify(transactionRepository).save(any());
        verify(notificationService).send(eq(userId), any(), anyString(), anyString());
    }

    @Test
    void createOrder_accountNotOwned_throwsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        TradeOrderRequest request = new TradeOrderRequest(
                accountId, "SBER", OrderDirection.BUY,
                new BigDecimal("10"), new BigDecimal("250.00")
        );

        when(accountRepository.findByIdAndUserIdAndDeletedAtIsNull(accountId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(ForbiddenException.class);

        verify(brokerClient, never()).submitOrder(any(), any());
        verify(orderRepository, never()).save(any());
    }
}
