package com.investagg.dto.request;

import com.investagg.entity.enums.OrderDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TradeOrderRequest(
        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotBlank(message = "Ticker is required")
        String ticker,

        @NotNull(message = "Direction is required")
        OrderDirection direction,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        BigDecimal qty,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price
) {}
