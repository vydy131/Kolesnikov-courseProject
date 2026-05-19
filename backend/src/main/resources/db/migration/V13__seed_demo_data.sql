-- =============================================================================
-- Demo data for development / presentation
-- Login: demo@investagg.ru / Demo1234
-- =============================================================================

DO $$
DECLARE
    v_user_id        UUID := 'a1000000-0000-0000-0000-000000000001';
    v_portfolio_id   UUID := 'a2000000-0000-0000-0000-000000000001';
    v_account_tink   UUID := 'a3000000-0000-0000-0000-000000000001';
    v_account_sber   UUID := 'a3000000-0000-0000-0000-000000000002';
    v_broker_tink    UUID;
    v_broker_sber    UUID;
    v_order_1        UUID := 'a4000000-0000-0000-0000-000000000001';
    v_order_2        UUID := 'a4000000-0000-0000-0000-000000000002';
    v_order_3        UUID := 'a4000000-0000-0000-0000-000000000003';
    v_order_4        UUID := 'a4000000-0000-0000-0000-000000000004';
BEGIN

-- ─── User ──────────────────────────────────────────────────────────────────
INSERT INTO users (id, email, password, created_at)
VALUES (
    v_user_id,
    'demo@investagg.ru',
    '$2a$10$coABHguTwyCK3qB1f3NgRuDUBVRnqn9ULdScgH6NuA68YAeASGlJm',  -- Demo1234
    NOW() - INTERVAL '30 days'
);

-- ─── Portfolio ─────────────────────────────────────────────────────────────
INSERT INTO portfolio (id, user_id, currency, created_at)
VALUES (v_portfolio_id, v_user_id, 'RUB', NOW() - INTERVAL '30 days');

-- ─── Broker IDs (seeded in V11) ────────────────────────────────────────────
SELECT id INTO v_broker_tink FROM broker WHERE name = 'Tinkoff'  LIMIT 1;
SELECT id INTO v_broker_sber FROM broker WHERE name = 'Sberbank' LIMIT 1;

-- ─── Investment Accounts ───────────────────────────────────────────────────
INSERT INTO investment_account (id, user_id, broker_id, account_number, broker_token, account_status, synced_at, created_at)
VALUES
    (v_account_tink, v_user_id, v_broker_tink, 'T-20241001-001',
     'RiKlEHo7PFGWrs6qRCIPmHVk0UdiJW8/aPqpYhD1ZAqpw9cspM+GzWm31KHF8A==',
     'ACTIVE', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '29 days'),
    (v_account_sber, v_user_id, v_broker_sber, 'S-BR-0042917',
     'yrBs7SEEtB+jXM7FF64rzkIMrqyu2pvXUwtZwgmRDPJ/krDPfr6ky7vCkw==',
     'ACTIVE', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '25 days');

-- ─── Assets ────────────────────────────────────────────────────────────────
INSERT INTO asset (id, portfolio_id, ticker, name, qty, avg_price, currency, updated_at)
VALUES
    (gen_random_uuid(), v_portfolio_id, 'SBER',  'Сбербанк',       20, 285.40, 'RUB', NOW()),
    (gen_random_uuid(), v_portfolio_id, 'YNDX',  'Яндекс',          5, 3870.00,'RUB', NOW()),
    (gen_random_uuid(), v_portfolio_id, 'GAZP',  'Газпром',        50, 162.30, 'RUB', NOW()),
    (gen_random_uuid(), v_portfolio_id, 'LKOH',  'Лукойл',          3, 7120.00,'RUB', NOW()),
    (gen_random_uuid(), v_portfolio_id, 'TCSG',  'TCS Group',      10, 2940.00,'RUB', NOW());

-- ─── Trade Orders ──────────────────────────────────────────────────────────
INSERT INTO trade_order (id, account_id, ticker, direction, qty, price, order_status, broker_order_id, placed_at, filled_at)
VALUES
    (v_order_1, v_account_tink, 'SBER', 'BUY',  20, 285.40, 'FILLED',    'TCS-ORD-001', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
    (v_order_2, v_account_tink, 'YNDX', 'BUY',   5, 3870.00,'FILLED',    'TCS-ORD-002', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
    (v_order_3, v_account_sber, 'GAZP', 'BUY',  50, 162.30, 'FILLED',    'SBR-ORD-001', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (v_order_4, v_account_tink, 'SBER', 'SELL',  5, 301.00, 'PENDING',   'TCS-ORD-003', NOW() - INTERVAL '1 hour',  NULL);

-- ─── Transactions ──────────────────────────────────────────────────────────
INSERT INTO transaction (id, account_id, order_id, transaction_type, amount, currency, occurred_at)
VALUES
    (gen_random_uuid(), v_account_tink, v_order_1, 'BUY',  5708.00, 'RUB', NOW() - INTERVAL '20 days'),
    (gen_random_uuid(), v_account_tink, v_order_2, 'BUY', 19350.00, 'RUB', NOW() - INTERVAL '15 days'),
    (gen_random_uuid(), v_account_sber, v_order_3, 'BUY',  8115.00, 'RUB', NOW() - INTERVAL '10 days');

-- ─── Notifications ─────────────────────────────────────────────────────────
INSERT INTO notification (id, user_id, notification_type, title, body, is_read, created_at)
VALUES
    (gen_random_uuid(), v_user_id, 'ORDER_FILLED',    'Ордер исполнен',
     'BUY SBER x20 по 285.40 ₽ — исполнен',                        TRUE,  NOW() - INTERVAL '20 days'),
    (gen_random_uuid(), v_user_id, 'ORDER_FILLED',    'Ордер исполнен',
     'BUY YNDX x5 по 3870.00 ₽ — исполнен',                        TRUE,  NOW() - INTERVAL '15 days'),
    (gen_random_uuid(), v_user_id, 'ORDER_FILLED',    'Ордер исполнен',
     'BUY GAZP x50 по 162.30 ₽ — исполнен',                        FALSE, NOW() - INTERVAL '10 days'),
    (gen_random_uuid(), v_user_id, 'PRICE_ALERT',     'Цена достигла цели',
     'SBER торгуется выше 300 ₽',                                   FALSE, NOW() - INTERVAL '2 hours');

END $$;
