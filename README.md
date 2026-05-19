# InvestAgg — Investment Aggregator Platform

Финтех-система для агрегирования брокерских счётов в единый интерфейс: аналитика портфеля, торговые операции, уведомления и отчётность.

---

## Быстрый старт

```bash
# Запустить всё (DB + backend + Android-эмулятор)
./start.sh

# Запустить всё для iOS симулятора
./start.sh ios

# Только backend + БД (без мобильного приложения)
./start.sh backend

# Только Docker-контейнеры
./start.sh db

# Остановить всё
./start.sh stop
```

### Требования

| Инструмент | Версия | Назначение |
|------------|--------|------------|
| Docker Desktop | ≥ 4.x | PostgreSQL + pgAdmin |
| Java JDK | 17+ | Spring Boot backend |
| Node.js | 18+ | React Native / Metro |
| npm | 9+ | Зависимости мобильного приложения |
| Android Studio | последняя | Android-эмулятор (для Android) |
| Xcode | ≥ 15 | iOS-симулятор (только macOS) |
| CocoaPods | последняя | iOS-зависимости (только iOS) |

---

## Структура проекта

```
courseProject/
├── start.sh                  ← скрипт запуска всего проекта
├── docker-compose.yml        ← Postgres 15 + pgAdmin
├── CLAUDE.md                 ← инструкции для Claude Code
├── README.md                 ← этот файл
│
├── backend/                  ← Java 17 / Spring Boot 3.5
│   ├── build.gradle.kts
│   ├── gradlew
│   └── src/
│       ├── main/
│       │   ├── java/com/investagg/
│       │   │   ├── BackendApplication.java
│       │   │   ├── controller/       ← REST-эндпоинты
│       │   │   ├── service/          ← бизнес-логика
│       │   │   ├── repository/       ← JPA-репозитории
│       │   │   ├── entity/           ← JPA-сущности
│       │   │   ├── dto/
│       │   │   │   ├── request/      ← входящие DTO
│       │   │   │   └── response/     ← исходящие DTO
│       │   │   ├── client/           ← клиенты внешних API
│       │   │   ├── security/         ← JWT, AES, SecurityConfig
│       │   │   ├── exception/        ← исключения + GlobalExceptionHandler
│       │   │   └── config/           ← OpenAPI, Security конфиги
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/     ← Flyway миграции V1–V11
│       └── test/                     ← JUnit 5 + @WebMvcTest тесты
│
└── mobile/                   ← React Native / TypeScript / MobX
    ├── App.tsx
    ├── index.js
    └── src/
        ├── screens/          ← экраны приложения
        │   ├── auth/         ← LoginScreen, RegisterScreen
        │   ├── portfolio/    ← PortfolioScreen
        │   ├── accounts/     ← AccountListScreen, ConnectBrokerScreen
        │   ├── orders/       ← OrderListScreen, TradeOrderForm
        │   └── notifications/← NotificationScreen
        ├── stores/           ← MobX стейт (userStore, portfolioStore, …)
        ├── api/              ← Axios-обёртки (authApi, portfolioApi, …)
        ├── navigation/       ← React Navigation (RootNavigator, стеки, табы)
        ├── components/       ← переиспользуемые компоненты
        ├── types/            ← TypeScript-интерфейсы (api.ts)
        └── services/         ← tokenService (AsyncStorage)
```

---

## Что где лежит

### Backend — ключевые файлы

| Файл | Что делает |
|------|------------|
| `controller/AuthController.java` | `POST /auth/register`, `POST /auth/login` |
| `controller/AccountController.java` | Подключение/отключение брокерских счетов |
| `controller/PortfolioController.java` | `GET /portfolio/analytics` — P&L по портфелю |
| `controller/OrderController.java` | Создание/отмена торговых ордеров |
| `controller/SyncController.java` | `POST /sync/accounts/{id}` — ручная синхронизация |
| `controller/NotificationController.java` | Список и прочтение уведомлений |
| `controller/ReportController.java` | Генерация PDF/CSV отчётов (async) |
| `service/UserService.java` | Регистрация, поиск пользователя |
| `service/AuthService.java` | Проверка пароля, генерация JWT |
| `service/AccountService.java` | Подключение счёта (AES-encrypt токен), отключение |
| `service/SyncService.java` | Синхронизация позиций и транзакций от брокера |
| `service/AnalyticsService.java` | Расчёт P&L по текущим ценам (MarketClient) |
| `service/OrderService.java` | Отправка ордера в брокер, сохранение транзакции |
| `service/ReportService.java` | `@Async` генерация отчётов |
| `service/SyncScheduler.java` | `@Scheduled` авто-синхронизация каждые 15 минут |
| `security/JwtService.java` | HMAC-SHA256 токены: генерация и валидация |
| `security/AesEncryptionService.java` | AES-256-GCM шифрование брокерских токенов |
| `security/SecurityConfig.java` | Spring Security: STATELESS, permitAll для /auth/** |
| `db/migration/` | Flyway: V1–V11, создание всех таблиц + seed-брокеры |

### Mobile — ключевые файлы

| Файл | Что делает |
|------|------------|
| `navigation/RootNavigator.tsx` | Корневой навигатор: Auth-стек vs Main-стек |
| `navigation/AuthStack.tsx` | Login + Register |
| `navigation/MainTabs.tsx` | 4 таба: Portfolio, Orders, Accounts, Notifications |
| `stores/userStore.ts` | Авторизация, JWT в AsyncStorage, isAuthenticated |
| `stores/portfolioStore.ts` | Загрузка и хранение аналитики портфеля |
| `stores/accountStore.ts` | Счета + брокеры, connect/disconnect/sync |
| `stores/orderStore.ts` | Ордера с пагинацией, placeOrder, cancelOrder |
| `stores/notificationStore.ts` | Уведомления, unreadCount (computed), markRead |
| `api/apiClient.ts` | Axios + Bearer-заголовок + 401 → logout |
| `types/api.ts` | TypeScript-интерфейсы всех DTO |

---

## API

**Base URL:** `http://localhost:8080/api/v1`

**Swagger UI:** `http://localhost:8080/api/v1/swagger-ui.html`

### Основные эндпоинты

```
POST   /auth/register              — регистрация { email, password }
POST   /auth/login                 — вход → { accessToken, tokenType, expiresIn }

GET    /portfolio/analytics        — P&L портфеля с текущими ценами   [JWT]

GET    /accounts                   — список подключённых счетов        [JWT]
POST   /accounts/connect           — подключить брокерский счёт        [JWT]
DELETE /accounts/{id}              — отключить счёт (soft delete)      [JWT]
GET    /accounts/brokers           — список доступных брокеров         [JWT]

POST   /sync/accounts/{id}         — ручная синхронизация              [JWT]

GET    /orders                     — список ордеров (page, size)       [JWT]
POST   /orders                     — выставить ордер                   [JWT]
DELETE /orders/{id}                — отменить ордер                    [JWT]

GET    /notifications              — список уведомлений                [JWT]
PATCH  /notifications/{id}/read   — отметить как прочитанное           [JWT]

POST   /reports/generate           — создать отчёт (async, 202)       [JWT]
GET    /reports                    — список отчётов                    [JWT]
GET    /reports/{id}               — статус / download URL             [JWT]
```

---

## База данных

**Подключение:** `localhost:5432`, БД `investagg`, логин `investagg` / `investagg`

**pgAdmin:** `http://localhost:5050` — `admin@investagg.local` / `admin`

### Таблицы

| Таблица | Описание |
|---------|----------|
| `users` | Пользователи. Soft delete (`deleted_at`). |
| `broker` | Справочник брокеров (5 штук, seed V11). |
| `investment_account` | Привязанные счета. Токен — AES-256-GCM. Soft delete. |
| `portfolio` | Портфель 1:1 к пользователю. |
| `asset` | Позиции: ticker, qty, avg_price. |
| `trade_order` | Ордера BUY/SELL. Soft delete. |
| `transaction` | Финансовые транзакции. Soft delete. |
| `market_data` | Кэш рыночных котировок. |
| `notification` | Уведомления. Индекс на `is_read=FALSE`. |
| `report` | Метаданные отчётов (PDF/CSV). Статус: GENERATING → READY/FAILED. |

Все миграции: `backend/src/main/resources/db/migration/V1__*.sql` … `V11__*.sql`

---

## Запуск тестов

```bash
# Backend (JUnit 5)
cd backend
./gradlew test

# Отчёт о покрытии
./gradlew test jacocoTestReport
# → build/reports/jacoco/test/html/index.html

# Mobile (Jest)
cd mobile
npm test

# TypeScript проверка
npx tsc --noEmit

# Линтер
npm run lint
```

---

## Переменные окружения (backend)

Все переменные имеют дефолты для локальной разработки. Для продакшена задавайте через `.env` или системные переменные:

| Переменная | Дефолт | Описание |
|------------|--------|----------|
| `DB_HOST` | `localhost` | Хост PostgreSQL |
| `DB_PORT` | `5432` | Порт PostgreSQL |
| `DB_NAME` | `investagg` | Имя базы данных |
| `DB_USER` | `investagg` | Пользователь БД |
| `DB_PASSWORD` | `investagg` | Пароль БД |
| `JWT_SECRET` | dev-строка | HMAC-ключ для JWT (мин. 32 символа) |
| `JWT_EXPIRATION_MS` | `3600000` | Время жизни токена (мс) |
| `AES_KEY` | dev-строка | 32-байтовый ключ AES-256 |
| `SERVER_PORT` | `8080` | Порт сервера |

---

## Архитектура (PCMEF)

```
[Mobile]                           [Backend]
Presentation (screens/)
  ↓
State (stores/)       ──HTTP──►   Control (controller/)
  ↓                                  ↓
API Client (api/)                 Mediator (service/)
                                     ↓
                                  Entity (entity/)
                                     ↓
                                  Foundation (repository/, client/)
```

**Правило:** зависимости только сверху вниз. Controller никогда не вызывает Repository напрямую.

---

## Демо
demo@investagg.ru / Demo1234
