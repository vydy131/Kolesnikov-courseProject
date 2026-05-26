# Руководство администратора

## Требования к окружению

| Инструмент | Версия | Назначение |
|---|---|---|
| Docker Desktop | 4.x+ | PostgreSQL + pgAdmin |
| Java JDK | 17+ | Spring Boot backend |
| Node.js | 18+ | React Native / Metro |
| npm | 9+ | Зависимости mobile |
| Android Studio | latest | Android-эмулятор |
| Xcode | 15+ | iOS-симулятор (macOS only) |

---

## Установка и запуск

### Быстрый старт
```bash
# Запуск всего (DB + backend + Android)
./start.sh

# iOS-симулятор
./start.sh ios

# Только backend + БД
./start.sh backend

# Только Docker-контейнеры
./start.sh db

# Остановка
./stop.sh
```

### Ручной запуск

#### 1. База данных
```bash
docker compose up -d
# Дождаться healthcheck: pg_isready
```

#### 2. Backend
```bash
cd backend
./gradlew build -x test
./gradlew bootRun
```
Flyway автоматически применит миграции V1–V13 при старте.

#### 3. Mobile
```bash
cd mobile
npm install
npx react-native start --port 8082     # Metro
npx react-native run-android --port 8082  # или run-ios
```

Для Android-эмулятора: `adb reverse tcp:8082 tcp:8082`

---

## Конфигурация

### Переменные окружения (backend)

| Переменная | По умолчанию | Описание |
|---|---|---|
| DB_HOST | localhost | Хост PostgreSQL |
| DB_PORT | 5432 | Порт |
| DB_NAME | investagg | Имя БД |
| DB_USER | investagg | Пользователь |
| DB_PASSWORD | investagg | Пароль |
| JWT_SECRET | dev-строка (32+ символа) | HMAC-ключ JWT |
| JWT_EXPIRATION_MS | 3600000 | TTL токена (мс) |
| AES_KEY | dev-строка (32 байта hex) | Ключ AES-256 |
| SERVER_PORT | 8080 | Порт сервера |

### Мобильное приложение
- `API_BASE_URL`: `http://10.0.2.2:8080/api/v1` (файл `mobile/src/api/apiClient.ts`)
- Metro порт: 8082 (файл `mobile/package.json`)

---

## Доступы

| Сервис | URL | Логин | Пароль |
|---|---|---|---|
| API | http://localhost:8080/api/v1 | -- | JWT |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html | -- | -- |
| pgAdmin | http://localhost:5050 | admin@investagg.local | admin |
| PostgreSQL | localhost:5432 | investagg | investagg |

---

## Тестирование

```bash
cd backend

# Запуск тестов
./gradlew test

# Тесты + покрытие
./gradlew test jacocoTestReport
# Отчёт: build/reports/jacoco/html/index.html
```

---

## Мониторинг

- Логи backend: `tail -f backend/app.log`
- Логи Metro: консоль терминала
- Логи Docker: `docker compose logs -f postgres`

---

## Резервное копирование

```bash
# Дамп БД
docker compose exec postgres pg_dump -U investagg investagg > backup.sql

# Восстановление
docker compose exec -T postgres psql -U investagg investagg < backup.sql
```
