package com.investagg.dto.response;

import com.investagg.entity.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String body,
        boolean isRead,
        OffsetDateTime createdAt
) {}
