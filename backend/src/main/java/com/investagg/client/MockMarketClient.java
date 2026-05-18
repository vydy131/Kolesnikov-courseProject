package com.investagg.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock market data client returning static prices.
 * Replace with real market data provider (e.g. Tinkoff Invest API, MOEX).
 */
@Slf4j
@Component
public class MockMarketClient implements MarketClient {

    private static final Map<String, BigDecimal> PRICES = Map.of(
            "SBER",  new BigDecimal("295.00"),
            "GAZP",  new BigDecimal("168.50"),
            "LKOH",  new BigDecimal("7250.00"),
            "YNDX",  new BigDecimal("4100.00"),
            "GMKN",  new BigDecimal("14800.00"),
            "TATN",  new BigDecimal("710.00"),
            "ROSN",  new BigDecimal("540.00"),
            "NVTK",  new BigDecimal("1200.00")
    );

    @Override
    public Map<String, BigDecimal> getPrices(List<String> tickers) {
        log.info("[MockMarketClient] Getting prices for {} tickers", tickers.size());
        Map<String, BigDecimal> result = new HashMap<>();
        for (String ticker : tickers) {
            result.put(ticker, PRICES.getOrDefault(ticker, new BigDecimal("100.00")));
        }
        return result;
    }
}
