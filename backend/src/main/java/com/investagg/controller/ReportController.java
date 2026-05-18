package com.investagg.controller;

import com.investagg.dto.request.GenerateReportRequest;
import com.investagg.dto.response.ReportResponse;
import com.investagg.security.SecurityUtils;
import com.investagg.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Request report generation (async)")
    public ReportResponse generate(@Valid @RequestBody GenerateReportRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        return reportService.generateReport(securityUtils.getCurrentUserId(principal), request);
    }

    @GetMapping
    @Operation(summary = "List all reports")
    public List<ReportResponse> list(@AuthenticationPrincipal UserDetails principal) {
        return reportService.listReports(securityUtils.getCurrentUserId(principal));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report status")
    public ReportResponse get(@PathVariable UUID reportId,
                              @AuthenticationPrincipal UserDetails principal) {
        return reportService.getReport(securityUtils.getCurrentUserId(principal), reportId);
    }
}
