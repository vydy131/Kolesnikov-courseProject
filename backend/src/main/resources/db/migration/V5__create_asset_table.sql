CREATE TABLE asset (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID          NOT NULL,
    ticker       VARCHAR(20)   NOT NULL,
    name         VARCHAR(255),
    qty          NUMERIC(18,6) NOT NULL,
    avg_price    NUMERIC(18,6) NOT NULL,
    currency     CHAR(3)       NOT NULL DEFAULT 'RUB',
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_asset_portfolio           FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE RESTRICT,
    CONSTRAINT uq_asset_portfolio_ticker    UNIQUE (portfolio_id, ticker)
);

CREATE INDEX idx_asset_portfolio_id ON asset(portfolio_id);
CREATE INDEX idx_asset_ticker       ON asset(ticker);
