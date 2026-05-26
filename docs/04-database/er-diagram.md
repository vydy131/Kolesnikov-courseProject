# ER-диаграмма

## Таблицы (10)

| Таблица | Описание | Soft Delete |
|---|---|---|
| users | Пользователи | Да |
| broker | Справочник брокеров (5 записей, seed V11) | Нет |
| investment_account | Привязанные счета, AES-токен | Да |
| portfolio | Портфель 1:1 к пользователю | Нет |
| asset | Позиции: ticker, qty, avg_price | Нет |
| trade_order | Ордера BUY/SELL | Да |
| transaction | Финансовые транзакции | Да |
| market_data | Кэш рыночных котировок | Нет |
| notification | Уведомления (индекс на is_read=FALSE) | Нет |
| report | Метаданные отчётов (PDF/CSV) | Нет |

## Связи

| Связь | Тип | FK | ON DELETE |
|---|---|---|---|
| users -> investment_account | 1:N | user_id | RESTRICT |
| broker -> investment_account | 1:N | broker_id | RESTRICT |
| users -> portfolio | 1:1 | user_id (UNIQUE) | RESTRICT |
| portfolio -> asset | 1:N | portfolio_id | RESTRICT |
| investment_account -> trade_order | 1:N | account_id | RESTRICT |
| investment_account -> transaction | 1:N | account_id | RESTRICT |
| trade_order -> transaction | 1:0..1 | order_id | RESTRICT |
| users -> notification | 1:N | user_id | RESTRICT |
| users -> report | 1:N | user_id | RESTRICT |
| portfolio -> report | 1:N | portfolio_id | RESTRICT |

## PlantUML

ER-диаграмма доступна в файле `er_diagram.puml` в корне проекта.
