# Техническая спецификация

## Система

**Investment Aggregator Platform (InvestAgg)** — распределённая финтех-система для агрегации брокерских счетов.

## Архитектура

Распределённый PCMEF (Presentation — Control — Mediator — Entity — Foundation).

## Технологический стек

### Backend
| Технология | Версия | Назначение |
|---|---|---|
| Java | 17 | Язык |
| Spring Boot | 3.5.0 | Фреймворк |
| Spring Data JPA | 3.x | ORM |
| Spring Security | 6.x | Аутентификация/авторизация |
| PostgreSQL | 15 | СУБД |
| Flyway | 10.x | Миграции |
| Gradle | 8.x | Сборка |
| JaCoCo | -- | Покрытие тестов |
| OpenAPI / Swagger | 3.0 | Документация API |

### Mobile
| Технология | Версия | Назначение |
|---|---|---|
| React Native | 0.85.3 | Фреймворк |
| TypeScript | 5.x | Язык |
| MobX | 6.x | Управление состоянием |
| React Navigation | 7.x | Навигация |
| Axios | 1.x | HTTP-клиент |
| AsyncStorage | -- | Хранение JWT |

### Инфраструктура
| Компонент | Описание |
|---|---|
| Docker Compose | PostgreSQL 15 + pgAdmin 4 |
| start.sh / stop.sh | Скрипты запуска/остановки |
| Metro Bundler | Порт 8082 |

## Безопасность

| Механизм | Реализация |
|---|---|
| Аутентификация | JWT (HMAC-SHA256), TTL 1 час |
| Хэширование паролей | BCrypt, strength 12 |
| Шифрование токенов | AES-256-GCM, 12-байт IV |
| Защита API | Bearer-заголовок, CSRF отключён (stateless) |
| Обработка ошибок | GlobalExceptionHandler, без стек-трейсов |

## База данных

- 10 таблиц, 13 Flyway-миграций
- UUID PK, TIMESTAMPTZ, snake_case
- Soft delete для финансовых сущностей
- Индексы на FK и WHERE/ORDER BY полях

## API

- Base URL: `/api/v1`
- 7 контроллеров, 17 эндпоинтов
- Пагинация для списков (Page)
- Swagger UI: `/swagger-ui.html`
