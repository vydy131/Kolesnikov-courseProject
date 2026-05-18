CREATE TABLE transaction (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID          NOT NULL,
    order_id        UUID          DEFAULT NULL,
    transaction_type VARCHAR(20)  NOT NULL,
    amount          NUMERIC(18,6) NOT NULL,
    currency        CHAR(3)       NOT NULL DEFAULT 'RUB',
    occurred_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ   DEFAULT NULL,

    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES investment_account(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transaction_order   FOREIGN KEY (order_id)   REFERENCES trade_order(id)        ON DELETE RESTRICT
);

CREATE INDEX idx_transaction_account_id  ON transaction(account_id);
CREATE INDEX idx_transaction_order_id    ON transaction(order_id);
CREATE INDEX idx_transaction_type        ON transaction(transaction_type);
CREATE INDEX idx_transaction_occurred_at ON transaction(occurred_at DESC);
