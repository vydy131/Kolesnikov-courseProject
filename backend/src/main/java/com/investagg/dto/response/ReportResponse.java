package com.investagg.dto.response;

import com.investagg.entity.enums.ReportFormat;
import com.investagg.entity.enums.ReportType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        ReportType type,
        ReportFormat format,
        String status,
        String downloadUrl,
        OffsetDateTime periodFrom,
        OffsetDateTime periodTo,
        OffsetDateTime generatedAt
) {}
