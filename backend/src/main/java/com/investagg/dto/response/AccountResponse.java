package com.investagg.dto.response;

import com.investagg.entity.enums.AccountStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID brokerId,
        String brokerName,
        String accountNumber,
        AccountStatus status,
        OffsetDateTime syncedAt,
        OffsetDateTime createdAt
) {}
