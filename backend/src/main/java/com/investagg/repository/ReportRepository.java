package com.investagg.repository;

import com.investagg.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByUserIdOrderByGeneratedAtDesc(UUID userId);
    Optional<Report> findByIdAndUserId(UUID id, UUID userId);
}
