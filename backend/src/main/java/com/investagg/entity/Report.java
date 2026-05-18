package com.investagg.entity;

import com.investagg.entity.enums.ReportFormat;
import com.investagg.entity.enums.ReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_format", nullable = false, length = 10)
    private ReportFormat reportFormat = ReportFormat.PDF;

    @Column(name = "report_status", nullable = false, length = 20)
    private String reportStatus = "GENERATING";

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "period_from", nullable = false)
    private OffsetDateTime periodFrom;

    @Column(name = "period_to", nullable = false)
    private OffsetDateTime periodTo;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private OffsetDateTime generatedAt = OffsetDateTime.now();
}
