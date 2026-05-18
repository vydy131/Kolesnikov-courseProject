# Use Cases

## UC-001 — Register User

**Actor:** Anonymous visitor  
**Goal:** Create an account in the system  
**Trigger:** User submits registration form

**Flow (PCMEF):**
```
Presentation: Submit RegisterForm
  → API Client: POST /auth/register
    → Control: UserController.register()
      → Mediator: UserService.createUser(email, password)
        → [check email uniqueness]
        → [hash password with BCrypt]
        → Foundation: UserRepository.save(user)
```

**Success:** `201 Created` with UserResponse  
**Failure:** `409 Conflict` if email exists

---

## UC-002 — Connect Broker Account

**Actor:** Authenticated user  
**Goal:** Link a brokerage account to aggregate data  
**Trigger:** User submits broker token via Connect Account screen

**Flow:**
```
Presentation: ConnectBrokerScreen → submit
  → API Client: POST /accounts/connect
    → Control: AccountController.connect()
      → Mediator: AccountService.connectBrokerAccount(userId, brokerId, token)
        → [validate token against broker API]
        → [encrypt token with AES-256]
        → Foundation: AccountRepository.save(account)
        → [trigger initial sync asynchronously]
```

**Success:** `201 Created` with AccountResponse  
**Failure:** `400 Bad Request` if broker token invalid; `409` if account already connected

---

## UC-003 — View Portfolio Analytics

**Actor:** Authenticated user  
**Goal:** View aggregated portfolio performance and current asset values  
**Trigger:** User opens Portfolio screen

**Flow:**
```
Presentation: PortfolioScreen mounts
  → State (PortfolioStore.fetchAnalytics())
    → API Client: GET /portfolio/analytics
      → Control: PortfolioController.getAnalytics()
        → Mediator: AnalyticsService.buildAnalytics(userId)
          → Foundation: AssetRepository.findByPortfolioUserId(userId)
          → Client: MarketClient.getPrices(tickers)
          → [calculate: totalValue, P&L, percent change per asset]
          → return PortfolioAnalyticsResponse
```

**Success:** Portfolio analytics rendered in screen  
**Note:** Market prices fetched in real-time from external provider

---

## UC-004 — Place Trade Order

**Actor:** Authenticated user  
**Goal:** Execute a buy or sell order on a connected broker account  
**Trigger:** User submits trade form

**Flow:**
```
Presentation: TradeOrderForm → submit
  → API Client: POST /orders
    → Control: OrderController.createOrder()
      → Mediator: OrderService.createOrder(accountId, ticker, direction, qty, price)
        → Foundation: AccountRepository.findById(accountId)
        → [validate account belongs to user]
        → Client: BrokerClient.submitOrder(account, orderRequest)
        → Foundation: OrderRepository.save(order)
        → Foundation: TransactionRepository.save(transaction)
        → [publish notification: ORDER_PLACED]
```

**Success:** `201 Created` with TradeOrderResponse; notification sent  
**Failure:** `422` if broker rejects; `403` if account doesn't belong to user

---

## UC-005 — Sync Broker Data

**Actor:** System (scheduled) or User (manual trigger)  
**Goal:** Pull latest positions, balances, and transactions from broker  
**Trigger:** Scheduled job (every 15 min) or user taps "Sync Now"

**Flow:**
```
Scheduler / API Client: POST /sync/accounts/{accountId}
  → Control: SyncController.syncAccount()
    → Mediator: SyncService.syncBrokerAccount(accountId)
      → Foundation: AccountRepository.findById(accountId)
      → Client: BrokerClient.fetchPositions(account)
      → Client: BrokerClient.fetchTransactions(account)
      → Mediator: [reconcile positions with local asset records]
      → Foundation: AssetRepository.saveAll(assets)
      → Foundation: TransactionRepository.saveAll(transactions)
      → Foundation: AccountRepository.updateSyncedAt(accountId, now)
      → [publish notification if sync error]
```

**Success:** `202 Accepted`; assets/transactions updated  
**Failure:** Notification created with `SYNC_ERROR` type

---

## UC-006 — Generate Report

**Actor:** Authenticated user  
**Goal:** Download a performance or tax report for a given period  
**Trigger:** User submits report request

**Flow:**
```
Presentation: ReportScreen → submit
  → API Client: POST /reports/generate
    → Control: ReportController.generate()
      → Mediator: ReportService.generateReport(portfolioId, type, format, period)
        → Foundation: TransactionRepository.findByPeriod(portfolioId, from, to)
        → Foundation: AssetRepository.findByPortfolioId(portfolioId)
        → [calculate metrics for period]
        → [render PDF/CSV]
        → Foundation: ReportRepository.save(report)
```

**Success:** `202 Accepted`; report ready when status = `READY`  
**Download:** `GET /reports/{id}/download`

---

## UC-007 — Notifications

**Actor:** System (event-driven)  
**Goal:** Inform the user about important events  
**Trigger:** Business event (order filled, sync error, price alert)

**Flow:**
```
Mediator (any service) → NotificationService.send(userId, type, title, body)
  → Foundation: NotificationRepository.save(notification)
  → [optional: push notification to mobile via FCM]

Presentation: NotificationScreen
  → API Client: GET /notifications
    → Control: NotificationController.list()
      → Mediator: NotificationService.getForUser(userId)
        → Foundation: NotificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
```

---

## Use Case — Layer Mapping

| Use Case       | Key Service           | External Dependency      |
|----------------|-----------------------|--------------------------|
| UC-001         | UserService           | —                        |
| UC-002         | AccountService        | BrokerClient (validation)|
| UC-003         | AnalyticsService      | MarketClient             |
| UC-004         | OrderService          | BrokerClient             |
| UC-005         | SyncService           | BrokerClient             |
| UC-006         | ReportService         | —                        |
| UC-007         | NotificationService   | FCM (optional)           |
