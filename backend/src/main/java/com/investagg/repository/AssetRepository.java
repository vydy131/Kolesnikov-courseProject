package com.investagg.repository;

import com.investagg.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByPortfolioId(UUID portfolioId);
    Optional<Asset> findByPortfolioIdAndTicker(UUID portfolioId, String ticker);
}
