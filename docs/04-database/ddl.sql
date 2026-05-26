CREATE TABLE users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  DEFAULT NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE TABLE broker (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    api_base    VARCHAR(500) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

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

CREATE TABLE portfolio (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL UNIQUE,
    currency    CHAR(3)     NOT NULL DEFAULT 'RUB',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_portfolio_user_id ON portfolio(user_id);

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

CREATE TABLE notification (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title             VARCHAR(255) NOT NULL,
    body              TEXT,
    is_read           BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_unread  ON notification(user_id, is_read) WHERE is_read = FALSE;

CREATE TABLE report (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    portfolio_id UUID        NOT NULL,
    report_type  VARCHAR(50) NOT NULL,
    report_format VARCHAR(10) NOT NULL DEFAULT 'PDF',
    report_status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    file_path    VARCHAR(500),
    period_from  TIMESTAMPTZ NOT NULL,
    period_to    TIMESTAMPTZ NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_report_user      FOREIGN KEY (user_id)      REFERENCES users(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_report_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE RESTRICT
);

CREATE INDEX idx_report_user_id      ON report(user_id);
CREATE INDEX idx_report_portfolio_id ON report(portfolio_id);

