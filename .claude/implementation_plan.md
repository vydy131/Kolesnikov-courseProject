# Implementation Plan — Investment Aggregator Platform

## Содержание

1. [Phase 1 — Project Bootstrap](#phase-1--project-bootstrap)
2. [Phase 2 — Database Migrations](#phase-2--database-migrations)
3. [Phase 3 — Entity & Repository Layer](#phase-3--entity--repository-layer)
4. [Phase 4 — Security Infrastructure](#phase-4--security-infrastructure)
5. [Phase 5 — Cross-cutting Infrastructure](#phase-5--cross-cutting-infrastructure)
6. [Phase 6 — UC-001: Authentication](#phase-6--uc-001-authentication)
7. [Phase 7 — UC-002: Broker Accounts](#phase-7--uc-002-broker-accounts)
8. [Phase 8 — UC-005: Broker Sync](#phase-8--uc-005-broker-sync)
9. [Phase 9 — UC-003: Portfolio Analytics](#phase-9--uc-003-portfolio-analytics)
10. [Phase 10 — UC-004 + UC-007: Trade Orders & Notifications](#phase-10--uc-004--uc-007-trade-orders--notifications)
11. [Phase 11 — UC-006: Reports](#phase-11--uc-006-reports)
12. [Phase 12 — Mobile Bootstrap](#phase-12--mobile-bootstrap)
13. [Phase 13 — Mobile: Auth](#phase-13--mobile-auth)
14. [Phase 14 — Mobile: Portfolio](#phase-14--mobile-portfolio)
15. [Phase 15 — Mobile: Accounts & Sync](#phase-15--mobile-accounts--sync)
16. [Phase 16 — Mobile: Orders](#phase-16--mobile-orders)
17. [Phase 17 — Mobile: Notifications & Reports](#phase-17--mobile-notifications--reports)
18. [Phase 18 — Testing](#phase-18--testing)
19. [Phase 19 — Hardening & Polish](#phase-19--hardening--polish)
20. [Dependency Graph](#dependency-graph)

---

## Phase 1 — Project Bootstrap

**Цель:** настроить пустой, но компилируемый и запускаемый скелет обеих частей проекта.

### Backend

- [ ] Инициализировать Gradle-проект через Spring Initializr (или вручную)
  - Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `postgresql`, `flyway-core`, `lombok`, `springdoc-openapi-starter-webmvc-ui`
  - Java 17, Gradle (Kotlin DSL)
- [ ] Настроить `application.yml`:
  - datasource (PostgreSQL)
  - Flyway locations
  - JWT secret + expiry (из env-переменных)
  - OpenAPI path
- [ ] Docker Compose (`docker-compose.yml`) с сервисами:
  - `postgres:15` с volume, healthcheck
  - (опционально) `pgadmin`
- [ ] Проверить: `./gradlew bootRun` запускается без ошибок

### Mobile

- [ ] Инициализировать React Native проект с TypeScript шаблоном
  ```bash
  npx react-native init mobile --template react-native-template-typescript
  ```
- [ ] Установить базовые зависимости:
  - `@react-navigation/native`, `@react-navigation/stack`, `@react-navigation/bottom-tabs`
  - `mobx`, `mobx-react-lite`
  - `axios`
  - `react-native-keychain` (для хранения JWT)
- [ ] Настроить `tsconfig.json` со `strict: true`
- [ ] Проверить: Metro запускается, приложение открывается на симуляторе

**Артефакты:** `backend/build.gradle`, `backend/src/main/resources/application.yml`, `docker-compose.yml`, `mobile/package.json`

---

## Phase 2 — Database Migrations

**Цель:** создать полную схему PostgreSQL через Flyway в порядке зависимостей.

Файлы в `backend/src/main/resources/db/migration/`:

| # | Файл | Таблица | Зависит от |
|---|------|---------|------------|
| V1 | `V1__create_users_table.sql` | `users` | — |
| V2 | `V2__create_broker_table.sql` | `broker` | — |
| V3 | `V3__create_investment_account_table.sql` | `investment_account` | users, broker |
| V4 | `V4__create_portfolio_table.sql` | `portfolio` | users |
| V5 | `V5__create_asset_table.sql` | `asset` | portfolio |
| V6 | `V6__create_trade_order_table.sql` | `trade_order` | investment_account |
| V7 | `V7__create_transaction_table.sql` | `transaction` | investment_account, trade_order |
| V8 | `V8__create_market_data_table.sql` | `market_data` | — |
| V9 | `V9__create_notification_table.sql` | `notification` | users |
| V10 | `V10__create_report_table.sql` | `report` | users, portfolio |
| V11 | `V11__seed_brokers.sql` | (seed data) | broker |

- [ ] Написать все 11 файлов по схеме из `.claude/database_model.md`
- [ ] Проверить: `./gradlew bootRun` проходит Flyway migrate без ошибок
- [ ] Проверить схему через pgAdmin или psql

**Артефакты:** `V1`–`V11` SQL-файлы

---

## Phase 3 — Entity & Repository Layer

**Цель:** JPA-сущности и Spring Data репозитории для всех таблиц.

### Entities (`com.investagg.entity`)

- [ ] `User` — поля: id, email, password, createdAt, deletedAt; `@OneToMany` accounts, `@OneToOne` portfolio
- [ ] `Broker` — поля: id, name, apiBase, isActive
- [ ] `InvestmentAccount` — поля: id, user, broker, accountNumber, brokerToken, status, syncedAt, createdAt, deletedAt
- [ ] `Portfolio` — поля: id, user, currency, createdAt; `@OneToMany` assets
- [ ] `Asset` — поля: id, portfolio, ticker, name, qty, avgPrice, currency, updatedAt
- [ ] `TradeOrder` — поля: id, account, ticker, direction (enum), qty, price, status (enum), placedAt, filledAt, deletedAt
- [ ] `Transaction` — поля: id, account, order (nullable), type (enum), amount, currency, occurredAt, deletedAt
- [ ] `MarketData` — поля: id, ticker, price, currency, source, fetchedAt
- [ ] `Notification` — поля: id, user, type (enum), title, body, isRead, createdAt
- [ ] `Report` — поля: id, user, portfolio, type (enum), format (enum), filePath, periodFrom, periodTo, generatedAt

Перечисления (`com.investagg.entity.enums`):
- `OrderDirection`: BUY, SELL
- `OrderStatus`: PENDING, FILLED, CANCELLED, REJECTED
- `TransactionType`: DEPOSIT, WITHDRAWAL, BUY, SELL, DIVIDEND
- `NotificationType`: ORDER_FILLED, ORDER_CANCELLED, SYNC_ERROR, PRICE_ALERT
- `ReportType`: PERFORMANCE, TAX, STATEMENT
- `ReportFormat`: PDF, CSV
- `AccountStatus`: ACTIVE, REVOKED, ERROR

### Repositories (`com.investagg.repository`)

- [ ] `UserRepository` — `findByEmail`, `existsByEmail`
- [ ] `BrokerRepository` — `findByIsActiveTrue`
- [ ] `AccountRepository` — `findByUserIdAndDeletedAtIsNull`, `findByIdAndUserIdAndDeletedAtIsNull`
- [ ] `PortfolioRepository` — `findByUserId`
- [ ] `AssetRepository` — `findByPortfolioId`, `findByPortfolioIdAndTicker`
- [ ] `OrderRepository` — `findByAccountUserIdAndDeletedAtIsNull` (Page)
- [ ] `TransactionRepository` — `findByAccountIdAndOccurredAtBetween`
- [ ] `MarketDataRepository` — `findTopByTickerOrderByFetchedAtDesc`
- [ ] `NotificationRepository` — `findByUserIdOrderByCreatedAtDesc`, `countByUserIdAndIsReadFalse`
- [ ] `ReportRepository` — `findByUserIdOrderByGeneratedAtDesc`

**Артефакты:** 10 Entity-классов, 7 Enum-классов, 10 Repository-интерфейсов

---

## Phase 4 — Security Infrastructure

**Цель:** рабочий JWT-аутентификационный стек без привязки к конкретным эндпоинтам.

- [ ] `JwtService` — генерация и валидация токенов (HMAC-SHA256 или RS256)
  - `generateToken(UserDetails)` → `String`
  - `extractUsername(token)` → `String`
  - `isTokenValid(token, UserDetails)` → `boolean`
- [ ] `JwtAuthFilter extends OncePerRequestFilter` — читает заголовок `Authorization: Bearer`, валидирует токен, устанавливает `SecurityContextHolder`
- [ ] `SecurityConfig extends SecurityFilterChain`:
  - `permitAll`: `POST /api/v1/auth/**`
  - `authenticated`: всё остальное
  - Добавить `JwtAuthFilter` перед `UsernamePasswordAuthenticationFilter`
  - CSRF disabled (stateless API)
  - Session management: `STATELESS`
- [ ] `UserDetailsServiceImpl` — загружает `User` из `UserRepository` по email
- [ ] `BCryptPasswordEncoder` bean (strength = 12)
- [ ] `AesEncryptionService` — AES-256-GCM для шифрования broker tokens
  - `encrypt(plaintext)` → `String` (Base64)
  - `decrypt(ciphertext)` → `String`

**Артефакты:** `JwtService`, `JwtAuthFilter`, `SecurityConfig`, `UserDetailsServiceImpl`, `AesEncryptionService`

---

## Phase 5 — Cross-cutting Infrastructure

**Цель:** общие компоненты, которые используются во всех фазах.

- [ ] **Exception hierarchy** (`com.investagg.exception`):
  - `AppException extends RuntimeException` (base, с `HttpStatus`)
  - `EntityNotFoundException` → 404
  - `ConflictException` → 409
  - `ForbiddenException` → 403
  - `BusinessRuleException` → 422
- [ ] **GlobalExceptionHandler** (`@RestControllerAdvice`):
  - Перехватывает все `AppException` → `ErrorResponse`
  - Перехватывает `MethodArgumentNotValidException` → `ValidationErrorResponse` с полями
  - Перехватывает непредвиденные `Exception` → 500 без stack trace
- [ ] **ErrorResponse** DTO: `{ error, code, timestamp }`
- [ ] **OpenAPI Config** (`springdoc`):
  - Схема `BearerAuth` для Swagger UI
  - Базовый путь `/api/v1`
- [ ] **Base pagination wrapper** `PageResponse<T>`: `content`, `page`, `size`, `totalElements`

**Артефакты:** пакет `exception/`, `GlobalExceptionHandler`, `OpenApiConfig`, `PageResponse`

---

## Phase 6 — UC-001: Authentication

**Цель:** регистрация и вход пользователя — первый рабочий end-to-end поток.

### DTOs

- [ ] `RegisterRequest`: `email` (@Email, @NotBlank), `password` (@Size(min=8))
- [ ] `LoginRequest`: `email`, `password`
- [ ] `UserResponse`: `id`, `email`, `createdAt`
- [ ] `TokenResponse`: `accessToken`, `tokenType = "Bearer"`, `expiresIn`

### Service

- [ ] `UserService`:
  - `createUser(RegisterRequest)` → `UserResponse`
    - проверить `existsByEmail` → `ConflictException` если true
    - `bcrypt.encode(password)`
    - сохранить, создать пустой `Portfolio` для пользователя
    - вернуть `UserResponse`
  - `getUserById(UUID)` → `UserResponse`

- [ ] `AuthService`:
  - `login(LoginRequest)` → `TokenResponse`
    - найти пользователя по email → `EntityNotFoundException` если нет
    - проверить пароль → `AppException(401)` если неверный
    - сгенерировать JWT → вернуть `TokenResponse`

### Controller

- [ ] `AuthController` (`/api/v1/auth`):
  - `POST /register` → `201 UserResponse`
  - `POST /login` → `200 TokenResponse`

**Проверка:** регистрация + логин через Swagger UI, JWT проверяется на защищённом эндпоинте.

---

## Phase 7 — UC-002: Broker Accounts

**Цель:** подключение брокерского счёта.

### External Client

- [ ] `BrokerClient` интерфейс:
  - `validateToken(String apiBase, String token)` → `boolean`
  - `fetchPositions(InvestmentAccount)` → `List<BrokerPosition>`
  - `fetchTransactions(InvestmentAccount, LocalDate from)` → `List<BrokerTransaction>`
  - `submitOrder(InvestmentAccount, BrokerOrderRequest)` → `String` (broker order ID)
  - `cancelOrder(InvestmentAccount, String brokerOrderId)` → `void`
- [ ] `MockBrokerClient implements BrokerClient` — возвращает статичные тестовые данные; используется по умолчанию до появления реального брокера

### DTOs

- [ ] `ConnectAccountRequest`: `brokerId`, `accountNumber`, `brokerToken`
- [ ] `AccountResponse`: `id`, `brokerName`, `accountNumber`, `status`, `syncedAt`

### Service

- [ ] `AccountService`:
  - `connectBrokerAccount(UUID userId, ConnectAccountRequest)` → `AccountResponse`
    - проверить уникальность (userId + brokerId + accountNumber) → `ConflictException`
    - `brokerClient.validateToken(...)` → `AppException(400)` если invalid
    - `aesService.encrypt(token)` → сохранить зашифрованным
    - сохранить `InvestmentAccount`
  - `getAccounts(UUID userId)` → `List<AccountResponse>`
  - `disconnectAccount(UUID userId, UUID accountId)` — soft delete

### Controller

- [ ] `AccountController` (`/api/v1/accounts`):
  - `POST /connect` → `201`
  - `GET /` → `200 List`
  - `DELETE /{accountId}` → `204`

---

## Phase 8 — UC-005: Broker Sync

**Цель:** синхронизация позиций и транзакций от брокера.

### External Client

- [ ] `MarketClient` интерфейс:
  - `getPrices(List<String> tickers)` → `Map<String, BigDecimal>`
  - `getHistory(String ticker, DateRange range)` → `List<PricePoint>`
- [ ] `MockMarketClient implements MarketClient` — возвращает статичные цены

### Service

- [ ] `SyncService`:
  - `syncBrokerAccount(UUID accountId)` → `SyncResult`
    - получить `InvestmentAccount` (расшифровать токен)
    - `brokerClient.fetchPositions(account)` → обновить `Asset` записи
    - `brokerClient.fetchTransactions(account, lastSyncDate)` → сохранить новые `Transaction`
    - обновить `account.syncedAt`
    - при ошибке — создать `Notification(SYNC_ERROR)`
  - `syncAllAccounts(UUID userId)` — вызов для каждого активного счёта
- [ ] `SyncScheduler` (`@Scheduled(fixedDelay = 900_000)`) — синхронизация всех активных аккаунтов каждые 15 минут

### Controller

- [ ] `SyncController` (`/api/v1/sync`):
  - `POST /accounts/{accountId}` → `202 SyncStatusResponse`

---

## Phase 9 — UC-003: Portfolio Analytics

**Цель:** агрегированная аналитика портфеля с живыми ценами.

### DTOs

- [ ] `PortfolioAnalyticsResponse`: `totalValue`, `currency`, `profitLoss`, `profitLossPercent`, `assets[]`, `updatedAt`
- [ ] `AssetAnalyticsItem`: `ticker`, `name`, `qty`, `avgPrice`, `currentPrice`, `totalValue`, `profitLoss`, `profitLossPercent`

### Service

- [ ] `AnalyticsService`:
  - `buildAnalytics(UUID userId)` → `PortfolioAnalyticsResponse`
    - получить Portfolio пользователя
    - собрать список тикеров из Asset
    - `marketClient.getPrices(tickers)`
    - рассчитать для каждого Asset: `currentValue = qty × price`, `pl = currentValue − qty × avgPrice`
    - рассчитать суммарные `totalValue`, `profitLoss`, `profitLossPercent`

### Controller

- [ ] `PortfolioController` (`/api/v1/portfolio`):
  - `GET /analytics` → `200 PortfolioAnalyticsResponse`

---

## Phase 10 — UC-004 + UC-007: Trade Orders & Notifications

**Цель:** выставление ордера с автоматическим уведомлением.

### DTOs

- [ ] `TradeOrderRequest`: `accountId`, `ticker`, `direction`, `qty` (@Positive), `price` (@Positive)
- [ ] `TradeOrderResponse`: `id`, `ticker`, `direction`, `qty`, `price`, `status`, `placedAt`
- [ ] `NotificationResponse`: `id`, `type`, `title`, `body`, `isRead`, `createdAt`

### Services

- [ ] `NotificationService` (реализовать первым, т.к. используется другими сервисами):
  - `send(UUID userId, NotificationType, String title, String body)`
  - `getForUser(UUID userId)` → `List<NotificationResponse>`
  - `markRead(UUID userId, UUID notificationId)`

- [ ] `OrderService`:
  - `createOrder(UUID userId, TradeOrderRequest)` → `TradeOrderResponse`
    - найти аккаунт, проверить владельца
    - `brokerClient.submitOrder(account, request)` → получить brokerOrderId
    - сохранить `TradeOrder` (status = PENDING)
    - сохранить `Transaction` (type = BUY/SELL)
    - `notificationService.send(userId, ORDER_PLACED, ...)`
  - `getOrders(UUID userId, Pageable)` → `Page<TradeOrderResponse>`
  - `cancelOrder(UUID userId, UUID orderId)`

### Controllers

- [ ] `OrderController` (`/api/v1/orders`):
  - `POST /` → `201`
  - `GET /` → `200 Page`
- [ ] `NotificationController` (`/api/v1/notifications`):
  - `GET /` → `200 List`
  - `PATCH /{id}/read` → `200`

---

## Phase 11 — UC-006: Reports

**Цель:** генерация PDF/CSV-отчётов по портфелю.

### Dependencies (добавить в `build.gradle`)

- `com.itextpdf:itext7-core` — PDF генерация
- или Apache POI для CSV/Excel

### DTOs

- [ ] `GenerateReportRequest`: `type`, `format`, `periodFrom`, `periodTo`
- [ ] `ReportResponse`: `id`, `type`, `format`, `status`, `downloadUrl`, `generatedAt`

### Service

- [ ] `ReportService`:
  - `generateReport(UUID userId, GenerateReportRequest)` → `ReportResponse`
    - создать `Report` (status = GENERATING) → вернуть `202` сразу
    - запустить async (`@Async`):
      - получить транзакции и активы за период
      - рассчитать метрики
      - рендерить PDF/CSV в файл (или S3, или локальный volume)
      - обновить `Report.status = READY`, `filePath`
  - `getReport(UUID userId, UUID reportId)` → `ReportResponse`

### Controller

- [ ] `ReportController` (`/api/v1/reports`):
  - `POST /generate` → `202`
  - `GET /{reportId}` → `200`
  - `GET /{reportId}/download` → `200 Resource` (file stream)

---

## Phase 12 — Mobile Bootstrap

**Цель:** скелет мобильного приложения с навигацией.

- [ ] Настроить React Navigation:
  - `RootNavigator`: различает Auth Stack / Main Stack
  - `AuthStack`: Login, Register
  - `MainTabs` (bottom tabs): Portfolio, Orders, Accounts, Notifications
  - `MainStack` (оверлеи): ConnectBroker, TradeOrderForm, ReportList
- [ ] `apiClient.ts` — Axios instance:
  - `baseURL = process.env.API_BASE_URL`
  - Request interceptor: добавляет `Authorization: Bearer <token>` из Keychain
  - Response interceptor: при `401` → logout
- [ ] `TokenService.ts` — хранение/получение JWT через `react-native-keychain`
- [ ] `rootStore.ts` — единый экспорт всех MobX stores
- [ ] Настроить Jest + `@testing-library/react-native`

---

## Phase 13 — Mobile: Auth ✅

- [x] `UserStore`: `register()`, `login()`, `logout()`, `isAuthenticated` (computed)
- [x] `authApi.ts`: `register(email, password)`, `login(email, password)`
- [x] `LoginScreen`: форма email + пароль, кнопка "Войти", ссылка на регистрацию
- [x] `RegisterScreen`: форма email + пароль, кнопка "Зарегистрироваться"
- [x] Обработка ошибок: `409` → "Email уже занят", `401` → "Неверные данные"

---

## Phase 14 — Mobile: Portfolio ✅

- [x] `PortfolioStore`: `analytics`, `loading`, `error`, `fetchAnalytics()`
- [x] `portfolioApi.ts`: `getAnalytics()`
- [x] `PortfolioScreen`:
  - Суммарная стоимость портфеля + P&L
  - `FlatList` с `AssetCard` для каждой позиции
  - Pull-to-refresh
- [x] `AssetCard`: ticker, текущая цена, P&L цветом (зелёный/красный)

---

## Phase 15 — Mobile: Accounts & Sync ✅

- [x] `AccountStore`: `accounts`, `fetchAccounts()`, `connectAccount()`, `disconnectAccount()`, `syncAccount()`
- [x] `accountApi.ts`: все 4 метода
- [x] `AccountListScreen`: список подключённых брокеров с датой синхронизации
- [x] `ConnectBrokerScreen`: выбор брокера из списка + поле токена
- [x] Кнопка "Синхронизировать" → `POST /sync/accounts/{id}`

---

## Phase 16 — Mobile: Orders ✅

- [x] `OrderStore`: `orders`, `fetchOrders()`, `placeOrder()`, `cancelOrder()`
- [x] `orderApi.ts`
- [x] `OrderListScreen`: пагинированный список ордеров с фильтром по статусу
- [x] `TradeOrderForm`: выбор аккаунта, тикер, BUY/SELL, qty, price

---

## Phase 17 — Mobile: Notifications & Reports ✅

- [x] `NotificationStore`: `notifications`, `unreadCount` (computed), `fetchNotifications()`, `markRead()`
- [x] `NotificationScreen`: список, непрочитанные выделены; tap → markRead
- [x] Badge на tab icon с `unreadCount`

---

## Phase 18 — Testing ✅

### Backend (JUnit 5 + Mockito)

- [x] `UserServiceTest` — createUser (happy path, duplicate email, password encoded)
- [x] `AuthServiceTest` — login (valid, wrong password, not found)
- [x] `AccountServiceTest` — connect (happy, duplicate, invalid token, broker not found), disconnect (forbidden, soft-delete)
- [x] `AnalyticsServiceTest` — buildAnalytics (empty portfolio, P&L calculation, price fallback, not found)
- [x] `OrderServiceTest` — createOrder (happy, forbidden account)
- [x] `UserRepositoryTest` (@DataJpaTest) — findByEmail, existsByEmail, soft-delete awareness
- [x] `AuthControllerTest` (@WebMvcTest) — register 201, 409, validation 400, login 200, missing field 400
- [x] `AccountControllerTest` (@WebMvcTest) — connect 201, 409, 400, list authenticated, list 401
- [x] `OrderControllerTest` (@WebMvcTest) — create 201, missing ticker 400, negative qty 400, paginated list, 401

---

## Phase 19 — Hardening & Polish ✅

- [x] `@Operation`, `@Tag`, `@SecurityRequirement` на всех контроллерах (OpenAPI) — было готово
- [x] PCMEF-нарушение исправлено: `SyncController` больше не обращается к `InvestmentAccountRepository` напрямую — через `AccountService.validateOwnership()`
- [x] N+1 в `SyncService.syncAllActiveAccounts()` исправлено: заменён `findAll()` + фильтрация на целевой запрос `findByDeletedAtIsNullAndAccountStatusNot()`
- [x] `GlobalExceptionHandler` — stack trace в ответ не попадает, только `{error, code, timestamp}`
- [x] Брокерские токены: декрипт только в памяти во время вызова, сразу перешифровываются, в логи не пишутся
- [x] Финальный PCMEF-аудит: Controller → Service → Repository (no skips)

---

## Dependency Graph

```
Phase 1 (Bootstrap)
  └─► Phase 2 (Migrations)
        └─► Phase 3 (Entity/Repository)
              └─► Phase 4 (Security)
              └─► Phase 5 (Infrastructure)
                    ├─► Phase 6 (Auth)  ◄── ПЕРВЫЙ рабочий e2e поток
                    │     └─► Phase 7 (Accounts)
                    │           └─► Phase 8 (Sync)
                    │                 └─► Phase 9 (Analytics)
                    │                       └─► Phase 10 (Orders + Notifications)
                    │                             └─► Phase 11 (Reports)
                    └─► Phase 12 (Mobile Bootstrap)
                          └─► Phase 13 (Mobile Auth)
                                └─► Phase 14 (Portfolio)
                                └─► Phase 15 (Accounts)
                                └─► Phase 16 (Orders)
                                └─► Phase 17 (Notifications)

Phases 1–11 + 12–17 → Phase 18 (Testing)
Phase 18 → Phase 19 (Hardening)
```

---

## Статус

| Phase | Статус | Примечание |
|-------|--------|------------|
| 1 — Bootstrap | ✅ выполнена | Spring Boot 3.5.0, RN init, Docker Compose |
| 2 — Migrations | ✅ выполнена | V1–V11, все 10 таблиц + seed brokers |
| 3 — Entity/Repository | ✅ выполнена | 10 entity, 7 enum, 10 repository |
| 4 — Security | ✅ выполнена | JWT, BCrypt, AES-256, SecurityConfig |
| 5 — Infrastructure | ✅ выполнена | Exceptions, GlobalExceptionHandler, OpenAPI, PageResponse |
| 6 — Auth | ✅ выполнена | UserService, AuthService, AuthController — BUILD SUCCESSFUL |
| 7 — Accounts | ✅ выполнена | BrokerClient (Mock), AccountService, AccountController |
| 8 — Sync | ✅ выполнена | MarketClient (Mock), SyncService, SyncScheduler, SyncController |
| 9 — Analytics | ✅ выполнена | AnalyticsService (live prices + P&L calc), PortfolioController |
| 10 — Orders + Notifications | ✅ выполнена | OrderService, NotificationService, OrderController, NotificationController |
| 11 — Reports | ✅ выполнена | ReportService (@Async PDF), ReportController — BUILD SUCCESSFUL |
| 12 — Mobile Bootstrap | ⬜ не начата | |
| 13 — Mobile Auth | ⬜ не начата | |
| 14 — Mobile Portfolio | ⬜ не начата | |
| 15 — Mobile Accounts | ⬜ не начата | |
| 16 — Mobile Orders | ⬜ не начата | |
| 17 — Mobile Notifications | ⬜ не начата | |
| 18 — Testing | ⬜ не начата | |
| 19 — Hardening | ⬜ не начата | |
