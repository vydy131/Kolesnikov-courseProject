package com.investagg.service;

import com.investagg.client.MarketClient;
import com.investagg.dto.response.AssetAnalyticsItem;
import com.investagg.dto.response.PortfolioAnalyticsResponse;
import com.investagg.entity.Asset;
import com.investagg.entity.Portfolio;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final MarketClient marketClient;

    public PortfolioAnalyticsResponse buildAnalytics(UUID userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found for user: " + userId));

        List<Asset> assets = portfolio.getAssets();

        if (assets.isEmpty()) {
            return new PortfolioAnalyticsResponse(
                    BigDecimal.ZERO, portfolio.getCurrency(),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), OffsetDateTime.now()
            );
        }

        List<String> tickers = assets.stream().map(Asset::getTicker).toList();
        Map<String, BigDecimal> prices = marketClient.getPrices(tickers);

        List<AssetAnalyticsItem> items = assets.stream()
                .map(asset -> buildAssetItem(asset, prices))
                .toList();

        BigDecimal totalValue = items.stream()
                .map(AssetAnalyticsItem::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = assets.stream()
                .map(a -> a.getAvgPrice().multiply(a.getQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profitLoss = totalValue.subtract(totalCost);
        BigDecimal profitLossPercent = totalCost.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : profitLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new PortfolioAnalyticsResponse(
                totalValue.setScale(2, RoundingMode.HALF_UP),
                portfolio.getCurrency(),
                profitLoss.setScale(2, RoundingMode.HALF_UP),
                profitLossPercent,
                items,
                OffsetDateTime.now()
        );
    }

    private AssetAnalyticsItem buildAssetItem(Asset asset, Map<String, BigDecimal> prices) {
        BigDecimal currentPrice = prices.getOrDefault(asset.getTicker(), asset.getAvgPrice());
        BigDecimal totalValue = currentPrice.multiply(asset.getQty());
        BigDecimal cost = asset.getAvgPrice().multiply(asset.getQty());
        BigDecimal pl = totalValue.subtract(cost);
        BigDecimal plPct = cost.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : pl.divide(cost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new AssetAnalyticsItem(
                asset.getTicker(),
                asset.getName(),
                asset.getQty(),
                asset.getAvgPrice().setScale(2, RoundingMode.HALF_UP),
                currentPrice.setScale(2, RoundingMode.HALF_UP),
                totalValue.setScale(2, RoundingMode.HALF_UP),
                pl.setScale(2, RoundingMode.HALF_UP),
                plPct
        );
    }
}
