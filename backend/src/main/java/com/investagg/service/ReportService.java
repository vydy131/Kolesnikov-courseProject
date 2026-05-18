package com.investagg.service;

import com.investagg.dto.request.GenerateReportRequest;
import com.investagg.dto.response.ReportResponse;
import com.investagg.entity.Portfolio;
import com.investagg.entity.Report;
import com.investagg.entity.Transaction;
import com.investagg.entity.User;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.PortfolioRepository;
import com.investagg.repository.ReportRepository;
import com.investagg.repository.TransactionRepository;
import com.investagg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public ReportResponse generateReport(UUID userId, GenerateReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

        OffsetDateTime from = request.periodFrom().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to   = request.periodTo().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Report report = new Report();
        report.setUser(user);
        report.setPortfolio(portfolio);
        report.setReportType(request.type());
        report.setReportFormat(request.format());
        report.setReportStatus("GENERATING");
        report.setPeriodFrom(from);
        report.setPeriodTo(to);
        reportRepository.save(report);

        generateAsync(report.getId(), portfolio.getId(), from, to);

        return toResponse(report);
    }

    @Async
    public void generateAsync(UUID reportId, UUID portfolioId, OffsetDateTime from, OffsetDateTime to) {
        try {
            Thread.sleep(500); // simulate processing time

            Report report = reportRepository.findById(reportId).orElseThrow();

            List<Transaction> transactions = transactionRepository
                    .findByAccountIdAndOccurredAtBetweenAndDeletedAtIsNull(portfolioId, from, to);

            // In production: render PDF/CSV using iText7 here
            // For now: mark as READY with a placeholder path
            report.setReportStatus("READY");
            report.setFilePath("/reports/" + reportId + "." + report.getReportFormat().name().toLowerCase());
            reportRepository.save(report);

            log.info("Report {} generated ({} transactions)", reportId, transactions.size());
        } catch (Exception ex) {
            log.error("Report generation failed for {}", reportId, ex);
            reportRepository.findById(reportId).ifPresent(r -> {
                r.setReportStatus("FAILED");
                reportRepository.save(r);
            });
        }
    }

    public ReportResponse getReport(UUID userId, UUID reportId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ForbiddenException("Report not found or access denied"));
        return toResponse(report);
    }

    public List<ReportResponse> listReports(UUID userId) {
        return reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    private ReportResponse toResponse(Report r) {
        String downloadUrl = "READY".equals(r.getReportStatus())
                ? "/api/v1/reports/" + r.getId() + "/download"
                : null;
        return new ReportResponse(
                r.getId(), r.getReportType(), r.getReportFormat(),
                r.getReportStatus(), downloadUrl,
                r.getPeriodFrom(), r.getPeriodTo(), r.getGeneratedAt()
        );
    }
}
