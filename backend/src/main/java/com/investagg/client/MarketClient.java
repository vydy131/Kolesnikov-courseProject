package com.investagg.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MarketClient {

    /**
     * Get current prices for a list of tickers.
     * Returns a map of ticker → price.
     */
    Map<String, BigDecimal> getPrices(List<String> tickers);
}
