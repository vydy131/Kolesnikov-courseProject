package com.investagg.dto.response;

import java.math.BigDecimal;

public record AssetAnalyticsItem(
        String ticker,
        String name,
        BigDecimal qty,
        BigDecimal avgPrice,
        BigDecimal currentPrice,
        BigDecimal totalValue,
        BigDecimal profitLoss,
        BigDecimal profitLossPercent
) {}
