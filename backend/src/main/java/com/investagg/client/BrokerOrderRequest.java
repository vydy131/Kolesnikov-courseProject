package com.investagg.client;

import com.investagg.entity.enums.OrderDirection;

import java.math.BigDecimal;

public record BrokerOrderRequest(
        String ticker,
        OrderDirection direction,
        BigDecimal qty,
        BigDecimal price
) {}
