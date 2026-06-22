# Руководство администратора

## Содержание

1. [Обзор системы](#1-обзор-системы)
2. [Требования к окружению](#2-требования-к-окружению)
3. [Установка и запуск](#3-установка-и-запуск)
4. [Конфигурация](#4-конфигурация)
5. [Управление базой данных](#5-управление-базой-данных)
6. [Мониторинг и логирование](#6-мониторинг-и-логирование)
7. [Тестирование и качество кода](#7-тестирование-и-качество-кода)
8. [Безопасность](#8-безопасность)
9. [Резервное копирование и восстановление](#9-резервное-копирование-и-восстановление)
10. [Устранение неполадок](#10-устранение-неполадок)
11. [Доступы и учётные данные](#11-доступы-и-учётные-данные)

---

## 1. Обзор системы

**InvestAgg** — распределённая финтех-система, состоящая из трёх компонентов:

| Компонент | Технология | Порт |
|---|---|---|
| Backend API | Java 17, Spring Boot 3.5 | 8080 |
| База данных | PostgreSQL 15 (Docker) | 5432 |
| Мобильное приложение | React Native, TypeScript, MobX | Metro: 8082 |

Дополнительные сервисы:
- **pgAdmin** — веб-интерфейс для PostgreSQL (порт 5050)
- **Swagger UI** — документация REST API

### Архитектура

```
[Mobile App]  --HTTP/JSON-->  [Spring Boot API]  --JPA-->  [PostgreSQL]
                                    |
                              [Broker APIs]
                              [Market APIs]
```

---

## 2. Требования к окружению

### Обязательные

| Инструмент | Версия | Проверка |
|---|---|---|
| Docker Desktop | 4.x+ | `docker --version` |
| Java JDK | 17+ | `java -version` |
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |

### Для Android-разработки

| Инструмент | Версия | Проверка |
|---|---|---|
| Android Studio | latest | `adb --version` |
| Android SDK | API 33+ | Android Studio > SDK Manager |
| Эмулятор | Pixel 5 / API 33 | AVD Manager |

### Для iOS-разработки (только macOS)

| Инструмент | Версия | Проверка |
|---|---|---|
| Xcode | 15+ | `xcodebuild -version` |
| CocoaPods | latest | `pod --version` |

### Рекомендуемые ресурсы

| Ресурс | Минимум | Рекомендуется |
|---|---|---|
| RAM | 8 ГБ | 16 ГБ |
| Свободное место | 5 ГБ | 10 ГБ |
| ОС | macOS 13+ / Linux / Windows 10+ | macOS (для iOS) |

---

## 3. Установка и запуск

### 3.1. Автоматический запуск (рекомендуется)

```bash
# Запуск всего (DB + backend + Android-эмулятор)
./start.sh

# Запуск с iOS-симулятором
./start.sh ios

# Только backend + БД (без мобильного приложения)
./start.sh backend

# Только Docker-контейнеры (PostgreSQL + pgAdmin)
./start.sh db

# Остановка всех сервисов
./stop.sh
```

Скрипт `start.sh` автоматически:
1. Проверяет наличие утилит (docker, java, node, npm)
2. Запускает Docker Compose и ожидает healthcheck PostgreSQL
3. Собирает backend (`./gradlew build -x test`)
4. Запускает backend в фоне (PID сохраняется в `backend/.pid`)
5. Устанавливает npm-зависимости (при первом запуске)
6. Запускает Metro bundler на порту 8082
7. Настраивает `adb reverse tcp:8082 tcp:8082` для Android-эмулятора
8. Собирает и устанавливает мобильное приложение

### 3.2. Ручной запуск

#### Шаг 1: База данных

```bash
docker compose up -d
```

Дождитесь готовности:
```bash
docker compose exec -T postgres pg_isready -U investagg -d investagg
```

#### Шаг 2: Backend

```bash
cd backend
./gradlew build -x test
./gradlew bootRun
```

Flyway автоматически применит миграции V1–V13 при первом запуске. Сервер будет доступен на `http://localhost:8080/api/v1`.

#### Шаг 3: Мобильное приложение

```bash
cd mobile
npm install                                    # установка зависимостей
npx react-native start --port 8082             # Metro bundler
```

В отдельном терминале:
```bash
adb reverse tcp:8082 tcp:8082                  # только для Android-эмулятора
npx react-native run-android --port 8082       # или run-ios --port 8082
```

### 3.3. Остановка

```bash
./stop.sh
```

Скрипт останавливает:
- Backend (через PID-файл `backend/.pid`)
- Gradle-демоны
- Metro bundler
- Docker-контейнеры (`docker compose down`)

---

## 4. Конфигурация

### 4.1. Переменные окружения (backend)

Конфигурация задаётся в `backend/src/main/resources/application.yml` через переменные окружения:

| Переменная | По умолчанию | Описание |
|---|---|---|
| `DB_HOST` | localhost | Хост PostgreSQL |
| `DB_PORT` | 5432 | Порт PostgreSQL |
| `DB_NAME` | investagg | Имя базы данных |
| `DB_USER` | investagg | Пользователь БД |
| `DB_PASSWORD` | investagg | Пароль БД |
| `JWT_SECRET` | dev-строка (32+ символа) | HMAC-ключ подписи JWT-токенов |
| `JWT_EXPIRATION_MS` | 3600000 | Время жизни токена (мс), по умолчанию 1 час |
| `AES_KEY` | dev-строка (32 байта hex) | Ключ шифрования AES-256-GCM для брокерских токенов |
| `SERVER_PORT` | 8080 | Порт HTTP-сервера |

Для production-окружения задавайте переменные через `.env` файл или системные переменные.

### 4.2. Конфигурация мобильного приложения

| Параметр | Файл | Значение |
|---|---|---|
| API_BASE_URL | `mobile/src/api/apiClient.ts` | `http://10.0.2.2:8080/api/v1` |
| Metro port | `mobile/package.json` (scripts) | 8082 |

`10.0.2.2` — специальный адрес, маршрутизирующий к localhost хост-машины из Android-эмулятора.

### 4.3. Docker Compose

Файл `docker-compose.yml` в корне проекта:

| Сервис | Образ | Порт | Volume |
|---|---|---|---|
| postgres | postgres:15-alpine | 5432 | postgres_data |
| pgadmin | dpage/pgadmin4:latest | 5050 | -- |

Healthcheck: `pg_isready -U investagg -d investagg`, интервал 10 сек, 5 попыток.

---

## 5. Управление базой данных

### 5.1. Подключение через pgAdmin

1. Откройте `http://localhost:5050`
2. Логин: `admin@investagg.local`, пароль: `admin`
3. Добавьте сервер:
   - Host: `postgres` (имя Docker-контейнера)
   - Port: `5432`
   - Username: `investagg`
   - Password: `investagg`

### 5.2. Подключение через psql

```bash
docker compose exec postgres psql -U investagg -d investagg
```

### 5.3. Flyway-миграции

| Миграция | Описание |
|---|---|
| V1 | Таблица users |
| V2 | Таблица broker |
| V3 | Таблица investment_account |
| V4 | Таблица portfolio |
| V5 | Таблица asset |
| V6 | Таблица trade_order |
| V7 | Таблица transaction |
| V8 | Таблица market_data |
| V9 | Таблица notification |
| V10 | Таблица report |
| V11 | Seed-данные: 5 брокеров |
| V12 | Fix: CHAR(3) -> VARCHAR(3) для currency |
| V13 | Демо-данные (пользователь, счета, активы, ордера) |

Миграции применяются автоматически при запуске backend. Статус миграций:
```bash
docker compose exec postgres psql -U investagg -d investagg \
  -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY version;"
```

### 5.4. Структура таблиц

10 таблиц: users, broker, investment_account, portfolio, asset, trade_order, transaction, market_data, notification, report.

Финансовые таблицы (users, investment_account, trade_order, transaction) используют **soft delete** через поле `deleted_at`.

---

## 6. Мониторинг и логирование

### 6.1. Логи backend

```bash
# Если запущен через start.sh
tail -f backend/app.log

# Если запущен через gradlew bootRun — логи в консоли
```

### 6.2. Логи Docker

```bash
# Все сервисы
docker compose logs -f

# Только PostgreSQL
docker compose logs -f postgres
```

### 6.3. Логи мобильного приложения

API-клиент логирует все запросы и ответы в консоль Metro:
```
[API] --> POST http://10.0.2.2:8080/api/v1/auth/login {...}
[API] <-- 200 /auth/login {accessToken: "..."}
[API] <-- ERROR 401 /portfolio/analytics {error: "Unauthorized"}
```

### 6.4. Проверка состояния сервисов

| Проверка | Команда |
|---|---|
| PostgreSQL | `docker compose exec -T postgres pg_isready -U investagg` |
| Backend API | `curl -sf http://localhost:8080/api/v1/auth/login` |
| Swagger UI | Открыть `http://localhost:8080/api/v1/swagger-ui.html` |
| Metro bundler | `curl -sf http://localhost:8082/status` |

---

## 7. Тестирование и качество кода

### 7.1. Запуск тестов

```bash
cd backend
./gradlew test
```

40 тестов: 18 unit (JUnit 5 + Mockito), 2 интеграционных (@DataJpaTest), 16 контроллерных (@WebMvcTest), 1 контекстный.

### 7.2. Покрытие кода (JaCoCo)

```bash
./gradlew test jacocoTestReport
```

Отчёт: `backend/build/reports/jacoco/html/index.html`

### 7.3. Статический анализ (Checkstyle)

```bash
./gradlew checkstyleMain checkstyleTest
```

Отчёт: `backend/build/reports/checkstyle/main.html`

Конфигурация: `backend/config/checkstyle/checkstyle.xml` (Google Java Style, адаптированный).

---

## 8. Безопасность

### 8.1. Аутентификация

- **JWT (HMAC-SHA256)** — stateless токены, TTL 1 час
- **BCrypt (strength 12)** — хэширование паролей
- Публичные эндпоинты: POST `/auth/register`, POST `/auth/login`, Swagger UI
- Все остальные эндпоинты требуют заголовок `Authorization: Bearer <JWT>`

### 8.2. Шифрование

- **AES-256-GCM** — шифрование брокерских токенов перед сохранением в БД
- Каждая операция шифрования генерирует случайный 12-байтовый IV
- Ключ: переменная окружения `AES_KEY`

### 8.3. Защита данных

- CSRF отключён (stateless API, без cookies)
- JPA-сущности никогда не возвращаются клиенту — используются DTO
- GlobalExceptionHandler: структурированные ошибки без стек-трейсов
- Soft delete для финансовых сущностей

### 8.4. Рекомендации для production

- Сменить `JWT_SECRET` на случайную строку длиной 64+ символа
- Сменить `AES_KEY` на случайный 32-байтовый hex
- Сменить пароли PostgreSQL и pgAdmin
- Отключить Swagger UI (`springdoc.swagger-ui.enabled=false`)
- Включить HTTPS (TLS-терминация на reverse proxy)

---

## 9. Резервное копирование и восстановление

### 9.1. Дамп базы данных

```bash
# Полный дамп
docker compose exec postgres pg_dump -U investagg investagg > backup_$(date +%Y%m%d).sql

# Только данные (без DDL)
docker compose exec postgres pg_dump -U investagg --data-only investagg > data_backup.sql

# Только схема
docker compose exec postgres pg_dump -U investagg --schema-only investagg > schema_backup.sql
```

### 9.2. Восстановление

```bash
# Полное восстановление (в чистую БД)
docker compose exec -T postgres psql -U investagg investagg < backup_20260623.sql

# Восстановление в новую БД
docker compose exec postgres createdb -U investagg investagg_restored
docker compose exec -T postgres psql -U investagg investagg_restored < backup_20260623.sql
```

### 9.3. Сброс и пересоздание

```bash
# Полный сброс БД (удаляет все данные!)
docker compose down -v
docker compose up -d
# Flyway пересоздаст таблицы при запуске backend
```

---

## 10. Устранение неполадок

### Backend не запускается

**Ошибка:** `Connection refused: localhost:5432`
- PostgreSQL не запущен. Выполните `docker compose up -d` и дождитесь healthcheck.

**Ошибка:** `Schema-validation: wrong column type`
- Flyway-миграция не применилась. Проверьте `flyway_schema_history`. При необходимости выполните `docker compose down -v` и перезапустите.

**Ошибка:** `Address already in use: 8080`
- Порт 8080 занят другим процессом. Найдите: `lsof -i :8080`. Остановите или смените `SERVER_PORT`.

### Metro bundler не запускается

**Ошибка:** `EADDRINUSE: address already in use :::8082`
- Порт 8082 занят. Найдите процесс: `lsof -i :8082` и остановите его, или используйте `./stop.sh`.

### Android-эмулятор не подключается к API

- Убедитесь, что выполнен `adb reverse tcp:8082 tcp:8082`
- Проверьте, что Metro запущен на порту 8082
- Проверьте, что backend доступен: `curl http://localhost:8080/api/v1/auth/login`

### iOS-симулятор: ошибка сборки

- Выполните `cd mobile/ios && pod install && cd ..`
- Убедитесь, что Xcode Command Line Tools установлены: `xcode-select --install`

---

## 11. Доступы и учётные данные

### Сервисные учётные записи

| Сервис | URL | Логин | Пароль |
|---|---|---|---|
| REST API | http://localhost:8080/api/v1 | -- | JWT-токен |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html | -- | -- |
| pgAdmin | http://localhost:5050 | admin@investagg.local | admin |
| PostgreSQL | localhost:5432 | investagg | investagg |

### Демо-пользователь

| Параметр | Значение |
|---|---|
| Email | demo@investagg.ru |
| Пароль | Demo1234 |

### Файлы с чувствительными данными

| Файл | Содержимое | Хранение в Git |
|---|---|---|
| `application.yml` | Dev-ключи JWT и AES | Да (только dev) |
| `.env` (если создан) | Production-секреты | **Нет** (добавить в .gitignore) |
| `docker-compose.yml` | Пароли PostgreSQL/pgAdmin | Да (только dev) |
