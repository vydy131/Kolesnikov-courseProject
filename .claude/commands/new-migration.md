Create a new Flyway migration script for the Investment Aggregator Platform.

Description: $ARGUMENTS

Steps:

1. Find the highest existing migration version in `backend/src/main/resources/db/migration/` (e.g., `V3__...` → next is `V4`)
2. Create the file: `backend/src/main/resources/db/migration/V{n}__{snake_case_description}.sql`
3. Apply the following rules in the SQL:

**Required conventions:**
- Primary keys: `UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- Timestamps: `TIMESTAMP WITH TIME ZONE` (never `TIMESTAMP` without timezone)
- Soft delete columns: `deleted_at TIMESTAMPTZ DEFAULT NULL`
- All FK columns must have a corresponding `CREATE INDEX`
- All `UNIQUE` constraints must be named: `CONSTRAINT uq_{table}_{columns} UNIQUE (...)`
- All FK constraints must be named: `CONSTRAINT fk_{table}_{ref_table} FOREIGN KEY ...`
- Financial tables use `NUMERIC(18, 6)` for monetary amounts — never `FLOAT` or `DOUBLE`

**Migration file structure:**
```sql
-- V{n}__{description}.sql

CREATE TABLE {table_name} (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- columns here
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ DEFAULT NULL            -- only if soft-delete needed
);

-- Indexes
CREATE INDEX idx_{table}_{col} ON {table}({col});

-- Comments (optional but helpful for financial tables)
COMMENT ON COLUMN {table}.{col} IS 'Description';
```

Check `.claude/database_model.md` for the full schema reference and naming conventions.
Check `.claude/domain_model.md` to verify relationships are correctly modeled.
