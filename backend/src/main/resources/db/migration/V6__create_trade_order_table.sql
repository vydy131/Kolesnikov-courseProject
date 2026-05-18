CREATE TABLE trade_order (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID          NOT NULL,
    ticker          VARCHAR(20)   NOT NULL,
    direction       VARCHAR(10)   NOT NULL,
    qty             NUMERIC(18,6) NOT NULL,
    price           NUMERIC(18,6) NOT NULL,
    order_status    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    broker_order_id VARCHAR(255)  DEFAULT NULL,
    placed_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    filled_at       TIMESTAMPTZ   DEFAULT NULL,
    deleted_at      TIMESTAMPTZ   DEFAULT NULL,

    CONSTRAINT fk_trade_order_account FOREIGN KEY (account_id) REFERENCES investment_account(id) ON DELETE RESTRICT
);

CREATE INDEX idx_trade_order_account_id  ON trade_order(account_id);
CREATE INDEX idx_trade_order_ticker      ON trade_order(ticker);
CREATE INDEX idx_trade_order_status      ON trade_order(order_status);
