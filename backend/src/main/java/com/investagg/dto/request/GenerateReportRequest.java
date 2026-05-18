package com.investagg.dto.request;

import com.investagg.entity.enums.ReportFormat;
import com.investagg.entity.enums.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateReportRequest(
        @NotNull(message = "Report type is required")
        ReportType type,

        @NotNull(message = "Format is required")
        ReportFormat format,

        @NotNull(message = "Period start date is required")
        LocalDate periodFrom,

        @NotNull(message = "Period end date is required")
        LocalDate periodTo
) {}
