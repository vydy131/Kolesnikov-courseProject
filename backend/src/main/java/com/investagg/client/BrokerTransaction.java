package com.investagg.client;

import com.investagg.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BrokerTransaction(
        String externalId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        OffsetDateTime occurredAt
) {}
