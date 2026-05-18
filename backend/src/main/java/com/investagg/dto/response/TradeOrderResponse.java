package com.investagg.dto.response;

import com.investagg.entity.enums.OrderDirection;
import com.investagg.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TradeOrderResponse(
        UUID id,
        UUID accountId,
        String ticker,
        OrderDirection direction,
        BigDecimal qty,
        BigDecimal price,
        OrderStatus status,
        String brokerOrderId,
        OffsetDateTime placedAt,
        OffsetDateTime filledAt
) {}
