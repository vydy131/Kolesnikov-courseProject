package com.investagg.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        OffsetDateTime createdAt
) {}
