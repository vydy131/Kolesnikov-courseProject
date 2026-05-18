CREATE TABLE market_data (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    ticker      VARCHAR(20)   NOT NULL,
    price       NUMERIC(18,6) NOT NULL,
    currency    CHAR(3)       NOT NULL DEFAULT 'RUB',
    source      VARCHAR(100),
    fetched_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_data_ticker     ON market_data(ticker);
CREATE INDEX idx_market_data_fetched_at ON market_data(fetched_at DESC);
