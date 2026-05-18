package com.investagg.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SyncStatusResponse(
        UUID accountId,
        String status,
        OffsetDateTime startedAt
) {}
