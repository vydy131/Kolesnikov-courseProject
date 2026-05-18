CREATE TABLE investment_account (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    broker_id       UUID        NOT NULL,
    account_number  VARCHAR(100) NOT NULL,
    broker_token    TEXT        NOT NULL,
    account_status  VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    synced_at       TIMESTAMPTZ  DEFAULT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ  DEFAULT NULL,

    CONSTRAINT fk_investment_account_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE RESTRICT,
    CONSTRAINT fk_investment_account_broker FOREIGN KEY (broker_id) REFERENCES broker(id)  ON DELETE RESTRICT,
    CONSTRAINT uq_account_user_broker_num   UNIQUE (user_id, broker_id, account_number)
);

CREATE INDEX idx_investment_account_user_id   ON investment_account(user_id);
CREATE INDEX idx_investment_account_broker_id ON investment_account(broker_id);
