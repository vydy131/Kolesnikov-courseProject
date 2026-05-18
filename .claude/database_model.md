# Database Model — PostgreSQL

## Global Rules

- All primary keys: `UUID` with `DEFAULT gen_random_uuid()`
- All timestamps: `TIMESTAMP WITH TIME ZONE` (UTC stored, timezone-aware)
- Soft delete: `deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL` on financial entities
- Normalization: 3NF — no transitive dependencies, no repeated groups
- All FK columns must have an accompanying index
- Migrations managed by **Flyway** (`V{n}__{description}.sql`)

---

## Table Definitions

### users

```sql
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,           -- BCrypt hash
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ  DEFAULT NULL
);

CREATE INDEX idx_users_email ON users(email);
```

---

### broker

```sql
CREATE TABLE broker (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,    -- e.g. 'Tinkoff', 'Finam'
    api_base    VARCHAR(500) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

### investment_account

```sql
CREATE TABLE investment_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    broker_id       UUID NOT NULL REFERENCES broker(id) ON DELETE RESTRICT,
    account_number  VARCHAR(100) NOT NULL,
    broker_token    TEXT NOT NULL,               -- AES-256 encrypted
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | REVOKED | ERROR
    synced_at       TIMESTAMPTZ DEFAULT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ DEFAULT NULL,

    CONSTRAINT uq_account_broker UNIQUE (user_id, broker_id, account_number)
);

CREATE INDEX idx_investment_account_user_id   ON investment_account(user_id);
CREATE INDEX idx_investment_account_broker_id ON investment_account(broker_id);
```

---

### portfolio

```sql
CREATE TABLE portfolio (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    currency    CHAR(3) NOT NULL DEFAULT 'RUB',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_user_id ON portfolio(user_id);
```

---

### asset

```sql
CREATE TABLE asset (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolio(id) ON DELETE RESTRICT,
    ticker       VARCHAR(20) NOT NULL,
    name         VARCHAR(255),
    qty          NUMERIC(18, 6) NOT NULL,
    avg_price    NUMERIC(18, 6) NOT NULL,
    currency     CHAR(3) NOT NULL DEFAULT 'RUB',
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_asset_portfolio_ticker UNIQUE (portfolio_id, ticker)
);

CREATE INDEX idx_asset_portfolio_id ON asset(portfolio_id);
CREATE INDEX idx_asset_ticker       ON asset(ticker);
```

---

### trade_order

```sql
CREATE TABLE trade_order (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES investment_account(id) ON DELETE RESTRICT,
    ticker      VARCHAR(20) NOT NULL,
    direction   VARCHAR(10) NOT NULL,            -- BUY | SELL
    qty         NUMERIC(18, 6) NOT NULL,
    price       NUMERIC(18, 6) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING | FILLED | CANCELLED | REJECTED
    placed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    filled_at   TIMESTAMPTZ DEFAULT NULL,
    deleted_at  TIMESTAMPTZ DEFAULT NULL
);

CREATE INDEX idx_trade_order_account_id ON trade_order(account_id);
CREATE INDEX idx_trade_order_ticker     ON trade_order(ticker);
CREATE INDEX idx_trade_order_status     ON trade_order(status);
```

---

### transaction

```sql
CREATE TABLE transaction (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID NOT NULL REFERENCES investment_account(id) ON DELETE RESTRICT,
    order_id        UUID REFERENCES trade_order(id) ON DELETE RESTRICT,   -- nullable (manual tx)
    type            VARCHAR(20) NOT NULL,         -- DEPOSIT | WITHDRAWAL | BUY | SELL | DIVIDEND
    amount          NUMERIC(18, 6) NOT NULL,
    currency        CHAR(3) NOT NULL DEFAULT 'RUB',
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ DEFAULT NULL
);

CREATE INDEX idx_transaction_account_id ON transaction(account_id);
CREATE INDEX idx_transaction_order_id   ON transaction(order_id);
CREATE INDEX idx_transaction_type       ON transaction(type);
```

---

### market_data

```sql
CREATE TABLE market_data (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticker      VARCHAR(20) NOT NULL,
    price       NUMERIC(18, 6) NOT NULL,
    currency    CHAR(3) NOT NULL DEFAULT 'RUB',
    source      VARCHAR(100),
    fetched_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_data_ticker     ON market_data(ticker);
CREATE INDEX idx_market_data_fetched_at ON market_data(fetched_at DESC);
```

---

### notification

```sql
CREATE TABLE notification (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    type        VARCHAR(50) NOT NULL,             -- ORDER_FILLED | SYNC_ERROR | PRICE_ALERT
    title       VARCHAR(255) NOT NULL,
    body        TEXT,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_user_id   ON notification(user_id);
CREATE INDEX idx_notification_is_read   ON notification(user_id, is_read);
```

---

### report

```sql
CREATE TABLE report (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    portfolio_id UUID NOT NULL REFERENCES portfolio(id) ON DELETE RESTRICT,
    type         VARCHAR(50) NOT NULL,            -- PERFORMANCE | TAX | STATEMENT
    format       VARCHAR(10) NOT NULL DEFAULT 'PDF',
    file_path    VARCHAR(500),
    period_from  TIMESTAMPTZ NOT NULL,
    period_to    TIMESTAMPTZ NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_report_user_id      ON report(user_id);
CREATE INDEX idx_report_portfolio_id ON report(portfolio_id);
```

---

## Entity Relationship Summary

```
users (1) ──────────────── (M) investment_account ─── (M) broker (M)
users (1) ──────────────── (1) portfolio
portfolio (1) ──────────── (M) asset
investment_account (1) ─── (M) trade_order
investment_account (1) ─── (M) transaction
trade_order (1) ────────── (M) transaction  (nullable FK)
users (1) ──────────────── (M) notification
users (1) ──────────────── (M) report
portfolio (1) ──────────── (M) report
```

## Soft Delete Policy

Physical DELETE is **forbidden** for:
- `users`, `investment_account`, `trade_order`, `transaction`

All queries on these tables must include `WHERE deleted_at IS NULL`.
