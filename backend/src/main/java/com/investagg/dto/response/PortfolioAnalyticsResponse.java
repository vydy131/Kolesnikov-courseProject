package com.investagg.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PortfolioAnalyticsResponse(
        BigDecimal totalValue,
        String currency,
        BigDecimal profitLoss,
        BigDecimal profitLossPercent,
        List<AssetAnalyticsItem> assets,
        OffsetDateTime updatedAt
) {}
