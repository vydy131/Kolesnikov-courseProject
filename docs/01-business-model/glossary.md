# Глоссарий предметной области

## Сущности (Entities)

| Сущность | Описание |
|---|---|
| User | Пользователь системы (инвестор) |
| Broker | Брокер (поставщик торговых данных) |
| InvestmentAccount | Инвестиционный счёт, привязанный к брокеру |
| Portfolio | Портфель пользователя (совокупность активов) |
| Asset | Финансовый актив (позиция в портфеле) |
| TradeOrder | Торговая заявка (ордер BUY/SELL) |
| Transaction | Финансовая операция (покупка, продажа, дивиденд) |
| Report | Аналитический отчёт (PDF/CSV) |
| Notification | Уведомление пользователя |
| MarketData | Рыночные данные (котировки) |

## Перечисления (Enums)

| Enum | Значения |
|---|---|
| AccountStatus | ACTIVE, REVOKED, ERROR |
| OrderDirection | BUY, SELL |
| OrderStatus | PENDING, FILLED, CANCELLED, REJECTED |
| TransactionType | DEPOSIT, WITHDRAWAL, BUY, SELL, DIVIDEND |
| NotificationType | ORDER_FILLED, ORDER_CANCELLED, SYNC_ERROR, PRICE_ALERT |
| ReportType | PERFORMANCE, TAX, STATEMENT |
| ReportFormat | PDF, CSV |

## Ключевые термины

| Термин | Определение |
|---|---|
| P&L (Profit & Loss) | Прибыль/убыток по позиции или портфелю |
| Soft Delete | Логическое удаление через поле deleted_at (без физического удаления) |
| JWT | JSON Web Token — токен аутентификации |
| AES-256-GCM | Алгоритм шифрования для хранения брокерских токенов |
| BCrypt | Алгоритм хэширования паролей |
| PCMEF | Архитектурный паттерн: Presentation, Control, Mediator, Entity, Foundation |
| MobX | Библиотека реактивного управления состоянием |
| Flyway | Инструмент миграции схемы БД |
