package com.investagg.service;

import com.investagg.client.MarketClient;
import com.investagg.dto.response.PortfolioAnalyticsResponse;
import com.investagg.entity.Asset;
import com.investagg.entity.Portfolio;
import com.investagg.entity.User;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private MarketClient marketClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Portfolio buildPortfolio(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("u@example.com");

        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());
        portfolio.setUser(user);
        portfolio.setCreatedAt(OffsetDateTime.now());
        return portfolio;
    }

    private Asset buildAsset(Portfolio portfolio, String ticker, String avgPrice, String qty) {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setPortfolio(portfolio);
        asset.setTicker(ticker);
        asset.setAvgPrice(new BigDecimal(avgPrice));
        asset.setQty(new BigDecimal(qty));
        return asset;
    }

    @Test
    void buildAnalytics_emptyPortfolio_returnsZeros() {
        UUID userId = UUID.randomUUID();
        Portfolio portfolio = buildPortfolio(userId);
        portfolio.setAssets(List.of());
        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.of(portfolio));

        PortfolioAnalyticsResponse response = analyticsService.buildAnalytics(userId);

        assertThat(response.totalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.profitLoss()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.assets()).isEmpty();
    }

    @Test
    void buildAnalytics_withAssets_calculatesCorrectPnL() {
        UUID userId = UUID.randomUUID();
        Portfolio portfolio = buildPortfolio(userId);

        // SBER: bought 10 at 250, now at 300 => PnL = (300-250)*10 = +500
        Asset sber = buildAsset(portfolio, "SBER", "250.00", "10");
        portfolio.setAssets(List.of(sber));

        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.of(portfolio));
        when(marketClient.getPrices(List.of("SBER"))).thenReturn(Map.of("SBER", new BigDecimal("300.00")));

        PortfolioAnalyticsResponse response = analyticsService.buildAnalytics(userId);

        assertThat(response.totalValue()).isEqualByComparingTo("3000.00");
        assertThat(response.profitLoss()).isEqualByComparingTo("500.00");
        assertThat(response.profitLossPercent()).isEqualByComparingTo("20.00");
        assertThat(response.assets()).hasSize(1);
        assertThat(response.assets().getFirst().ticker()).isEqualTo("SBER");
    }

    @Test
    void buildAnalytics_priceNotInMarket_fallsBackToAvgPrice() {
        UUID userId = UUID.randomUUID();
        Portfolio portfolio = buildPortfolio(userId);

        Asset asset = buildAsset(portfolio, "UNKNOWN", "100.00", "5");
        portfolio.setAssets(List.of(asset));

        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.of(portfolio));
        when(marketClient.getPrices(List.of("UNKNOWN"))).thenReturn(Map.of());

        PortfolioAnalyticsResponse response = analyticsService.buildAnalytics(userId);

        // Falls back to avgPrice, so PnL = 0
        assertThat(response.profitLoss()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void buildAnalytics_portfolioNotFound_throwsEntityNotFound() {
        UUID userId = UUID.randomUUID();
        when(portfolioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.buildAnalytics(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
