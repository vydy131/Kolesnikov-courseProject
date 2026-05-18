package com.investagg.client;

import com.investagg.entity.InvestmentAccount;
import com.investagg.entity.enums.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mock broker client returning static test data.
 * Replace with real implementation per broker integration.
 */
@Slf4j
@Component
public class MockBrokerClient implements BrokerClient {

    @Override
    public boolean validateToken(String apiBase, String token) {
        log.info("[MockBrokerClient] Validating token for apiBase={}", apiBase);
        return token != null && !token.isBlank();
    }

    @Override
    public List<BrokerPosition> fetchPositions(InvestmentAccount account) {
        log.info("[MockBrokerClient] Fetching positions for account={}", account.getId());
        return List.of(
                new BrokerPosition("SBER", "Сбербанк", new BigDecimal("100"), new BigDecimal("280.00"), "RUB"),
                new BrokerPosition("GAZP", "Газпром",  new BigDecimal("50"),  new BigDecimal("170.00"), "RUB"),
                new BrokerPosition("LKOH", "Лукойл",   new BigDecimal("10"),  new BigDecimal("7100.00"), "RUB")
        );
    }

    @Override
    public List<BrokerTransaction> fetchTransactions(InvestmentAccount account, LocalDate from) {
        log.info("[MockBrokerClient] Fetching transactions for account={} since={}", account.getId(), from);
        return List.of(
                new BrokerTransaction(
                        UUID.randomUUID().toString(),
                        TransactionType.BUY,
                        new BigDecimal("28000.00"),
                        "RUB",
                        OffsetDateTime.now().minusDays(5)
                ),
                new BrokerTransaction(
                        UUID.randomUUID().toString(),
                        TransactionType.DIVIDEND,
                        new BigDecimal("1500.00"),
                        "RUB",
                        OffsetDateTime.now().minusDays(2)
                )
        );
    }

    @Override
    public String submitOrder(InvestmentAccount account, BrokerOrderRequest request) {
        String brokerOrderId = "MOCK-" + UUID.randomUUID();
        log.info("[MockBrokerClient] Submitted order {} {} x{} @{} → brokerOrderId={}",
                request.direction(), request.ticker(), request.qty(), request.price(), brokerOrderId);
        return brokerOrderId;
    }

    @Override
    public void cancelOrder(InvestmentAccount account, String brokerOrderId) {
        log.info("[MockBrokerClient] Cancelled order brokerOrderId={}", brokerOrderId);
    }
}
