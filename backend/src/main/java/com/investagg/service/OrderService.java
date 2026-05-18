package com.investagg.service;

import com.investagg.client.BrokerClient;
import com.investagg.client.BrokerOrderRequest;
import com.investagg.dto.request.TradeOrderRequest;
import com.investagg.dto.response.PageResponse;
import com.investagg.dto.response.TradeOrderResponse;
import com.investagg.entity.InvestmentAccount;
import com.investagg.entity.TradeOrder;
import com.investagg.entity.Transaction;
import com.investagg.entity.enums.NotificationType;
import com.investagg.entity.enums.OrderStatus;
import com.investagg.entity.enums.TransactionType;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.InvestmentAccountRepository;
import com.investagg.repository.TradeOrderRepository;
import com.investagg.repository.TransactionRepository;
import com.investagg.security.AesEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final TradeOrderRepository orderRepository;
    private final InvestmentAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BrokerClient brokerClient;
    private final AesEncryptionService aesService;
    private final NotificationService notificationService;

    @Transactional
    public TradeOrderResponse createOrder(UUID userId, TradeOrderRequest request) {
        InvestmentAccount account = accountRepository
                .findByIdAndUserIdAndDeletedAtIsNull(request.accountId(), userId)
                .orElseThrow(() -> new ForbiddenException("Account not found or access denied"));

        // Decrypt token for broker call
        String plainToken = aesService.decrypt(account.getBrokerToken());
        account.setBrokerToken(plainToken);

        String brokerOrderId = brokerClient.submitOrder(account,
                new BrokerOrderRequest(request.ticker(), request.direction(), request.qty(), request.price()));

        // Re-encrypt token
        account.setBrokerToken(aesService.encrypt(plainToken));

        TradeOrder order = new TradeOrder();
        order.setAccount(account);
        order.setTicker(request.ticker());
        order.setDirection(request.direction());
        order.setQty(request.qty());
        order.setPrice(request.price());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setBrokerOrderId(brokerOrderId);
        orderRepository.save(order);

        TransactionType txType = request.direction().name().equals("BUY")
                ? TransactionType.BUY : TransactionType.SELL;
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setOrder(order);
        tx.setTransactionType(txType);
        tx.setAmount(request.qty().multiply(request.price()));
        tx.setCurrency("RUB");
        transactionRepository.save(tx);

        notificationService.send(userId, NotificationType.ORDER_FILLED,
                "Order placed",
                String.format("%s %s x%.0f @%.2f — submitted",
                        request.direction(), request.ticker(), request.qty(), request.price()));

        return toResponse(order);
    }

    public PageResponse<TradeOrderResponse> getOrders(UUID userId, Pageable pageable) {
        return PageResponse.from(
                orderRepository.findByUserId(userId, pageable).map(this::toResponse)
        );
    }

    @Transactional
    public void cancelOrder(UUID userId, UUID orderId) {
        TradeOrder order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getAccount().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        InvestmentAccount account = order.getAccount();
        String plainToken = aesService.decrypt(account.getBrokerToken());
        account.setBrokerToken(plainToken);

        brokerClient.cancelOrder(account, order.getBrokerOrderId());
        account.setBrokerToken(aesService.encrypt(plainToken));

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        notificationService.send(userId, NotificationType.ORDER_CANCELLED,
                "Order cancelled",
                String.format("Order for %s x%.0f has been cancelled", order.getTicker(), order.getQty()));
    }

    private TradeOrderResponse toResponse(TradeOrder o) {
        return new TradeOrderResponse(
                o.getId(),
                o.getAccount().getId(),
                o.getTicker(),
                o.getDirection(),
                o.getQty(),
                o.getPrice(),
                o.getOrderStatus(),
                o.getBrokerOrderId(),
                o.getPlacedAt(),
                o.getFilledAt()
        );
    }
}
