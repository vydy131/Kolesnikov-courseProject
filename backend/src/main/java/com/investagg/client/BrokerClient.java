package com.investagg.client;

import com.investagg.entity.InvestmentAccount;

import java.time.LocalDate;
import java.util.List;

public interface BrokerClient {

    /**
     * Validate that the broker API token is accepted by the broker.
     */
    boolean validateToken(String apiBase, String token);

    /**
     * Fetch current positions (holdings) for the account.
     */
    List<BrokerPosition> fetchPositions(InvestmentAccount account);

    /**
     * Fetch transactions since the given date.
     */
    List<BrokerTransaction> fetchTransactions(InvestmentAccount account, LocalDate from);

    /**
     * Submit a trade order. Returns the broker-assigned order ID.
     */
    String submitOrder(InvestmentAccount account, BrokerOrderRequest request);

    /**
     * Cancel an order by its broker-assigned ID.
     */
    void cancelOrder(InvestmentAccount account, String brokerOrderId);
}
