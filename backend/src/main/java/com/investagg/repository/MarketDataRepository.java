package com.investagg.repository;

import com.investagg.entity.MarketData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {
    Optional<MarketData> findTopByTickerOrderByFetchedAtDesc(String ticker);
}
