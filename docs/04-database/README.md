# 04. База данных

## Обзор

PostgreSQL 15 с управлением схемой через Flyway (13 миграций V1–V13). Все таблицы используют snake_case, UUID PK с DEFAULT gen_random_uuid(), TIMESTAMPTZ для временных меток.

## Ключевые решения

- **Soft delete** для финансовых сущностей (users, investment_account, trade_order, transaction) через поле `deleted_at`
- **Индексы** на всех FK-столбцах и полях WHERE/ORDER BY
- **AES-256-GCM** шифрование broker_token в таблице investment_account
- **Пагинация** для всех списковых запросов

## Содержание

- [er-diagram.md](er-diagram.md) — ER-диаграмма
- [ddl.sql](ddl.sql) — DDL-скрипты создания таблиц
