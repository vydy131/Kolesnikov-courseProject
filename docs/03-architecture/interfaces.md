# Интерфейсы между слоями

## REST API (клиент <-> сервер)

Base URL: `/api/v1` | Формат: `application/json` | Аутентификация: `Bearer <JWT>`

### Публичные эндпоинты (без JWT)
| Метод | Путь | Описание | Статус |
|---|---|---|---|
| POST | /auth/register | Регистрация | 201 |
| POST | /auth/login | Авторизация | 200 |

### Защищённые эндпоинты (JWT required)
| Метод | Путь | Описание | Статус |
|---|---|---|---|
| GET | /portfolio/analytics | Аналитика портфеля | 200 |
| POST | /accounts/connect | Подключить счёт | 201 |
| GET | /accounts | Список счетов | 200 |
| DELETE | /accounts/{id} | Отключить счёт | 204 |
| GET | /accounts/brokers | Список брокеров | 200 |
| POST | /orders | Создать ордер | 201 |
| GET | /orders | Список ордеров (Page) | 200 |
| DELETE | /orders/{id} | Отменить ордер | 204 |
| POST | /sync/accounts/{id} | Ручная синхронизация | 202 |
| GET | /notifications | Список уведомлений | 200 |
| PATCH | /notifications/{id}/read | Пометить прочитанным | 200 |
| POST | /reports/generate | Создать отчёт (async) | 202 |
| GET | /reports/{id} | Статус отчёта | 200 |

### Коды ошибок
| HTTP | Код | Описание |
|---|---|---|
| 400 | VALIDATION_ERROR | Невалидное тело запроса |
| 401 | UNAUTHORIZED | Отсутствует/невалидный JWT |
| 403 | FORBIDDEN | Нет прав |
| 404 | NOT_FOUND | Ресурс не найден |
| 409 | CONFLICT | Дублирование |
| 500 | INTERNAL_ERROR | Внутренняя ошибка |

## Внутренние интерфейсы (сервер)

### Control -> Mediator
Контроллеры вызывают сервисы, передавая userId (извлечённый из JWT) и DTO.

### Mediator -> Foundation
Сервисы вызывают репозитории (JpaRepository) и внешние клиенты (BrokerClient, MarketClient).

### Mediator -> Entity
Сервисы создают и модифицируют JPA-сущности, но возвращают наружу только DTO.
