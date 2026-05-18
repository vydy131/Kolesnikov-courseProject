package com.investagg.client;

import java.math.BigDecimal;

public record BrokerPosition(
        String ticker,
        String name,
        BigDecimal qty,
        BigDecimal avgPrice,
        String currency
) {}
