-- Hibernate expects VARCHAR, but CHAR(3) is stored as bpchar in PostgreSQL.
-- Convert all currency columns to VARCHAR(3) to pass schema validation.

ALTER TABLE portfolio    ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE asset        ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE transaction  ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE market_data  ALTER COLUMN currency TYPE VARCHAR(3);
