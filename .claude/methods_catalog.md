# Methods Catalog

## Backend Service Methods

### UserService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| createUser | `UserResponse createUser(RegisterRequest request)` | Validate uniqueness, hash password, persist |
| getUserById | `UserResponse getUserById(UUID userId)` | Fetch user or throw 404 |
| deleteUser | `void deleteUser(UUID userId)` | Soft-delete via `deleted_at` |

### AccountService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| connectBrokerAccount | `AccountResponse connectBrokerAccount(UUID userId, ConnectAccountRequest req)` | Validate token, encrypt, persist |
| getAccounts | `List<AccountResponse> getAccounts(UUID userId)` | List user's active accounts |
| disconnectAccount | `void disconnectAccount(UUID userId, UUID accountId)` | Soft-delete, revoke token |

### AnalyticsService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| buildAnalytics | `PortfolioAnalyticsResponse buildAnalytics(UUID userId)` | Fetch assets, get live prices, calculate P&L |
| getAssetHistory | `List<AssetHistoryPoint> getAssetHistory(UUID userId, String ticker, DateRange range)` | Time-series for a single asset |

### OrderService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| createOrder | `TradeOrderResponse createOrder(UUID userId, TradeOrderRequest req)` | Validate, submit to broker, persist |
| getOrders | `Page<TradeOrderResponse> getOrders(UUID userId, OrderFilter filter, Pageable pageable)` | Paginated order list |
| cancelOrder | `void cancelOrder(UUID userId, UUID orderId)` | Cancel via broker API, update status |

### SyncService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| syncBrokerAccount | `SyncResult syncBrokerAccount(UUID accountId)` | Pull positions + transactions from broker |
| syncAllAccounts | `void syncAllAccounts(UUID userId)` | Trigger sync for all user accounts |

### NotificationService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| send | `void send(UUID userId, NotificationType type, String title, String body)` | Persist + optional push |
| getForUser | `List<NotificationResponse> getForUser(UUID userId)` | List notifications, latest first |
| markRead | `void markRead(UUID userId, UUID notificationId)` | Toggle `is_read = true` |

### ReportService (Mediator)

| Method | Signature | Description |
|--------|-----------|-------------|
| generateReport | `ReportResponse generateReport(UUID userId, GenerateReportRequest req)` | Async report generation |
| getReport | `ReportResponse getReport(UUID userId, UUID reportId)` | Status + download URL |

---

## External Client Methods

### BrokerClient (Foundation)

| Method | Signature | Description |
|--------|-----------|-------------|
| validateToken | `boolean validateToken(String brokerApiBase, String token)` | Check if token is valid |
| fetchPositions | `List<BrokerPosition> fetchPositions(InvestmentAccount account)` | Pull current holdings |
| fetchTransactions | `List<BrokerTransaction> fetchTransactions(InvestmentAccount account, LocalDate from)` | Pull transaction history |
| submitOrder | `String submitOrder(InvestmentAccount account, BrokerOrderRequest req)` | Place order, return broker order ID |
| cancelOrder | `void cancelOrder(InvestmentAccount account, String brokerOrderId)` | Cancel order via broker API |

### MarketClient (Foundation)

| Method | Signature | Description |
|--------|-----------|-------------|
| getPrices | `Map<String, BigDecimal> getPrices(List<String> tickers)` | Real-time prices for tickers |
| getHistory | `List<PricePoint> getHistory(String ticker, DateRange range)` | OHLCV historical data |

---

## Frontend Store Methods

### UserStore (MobX)

| Method | Description |
|--------|-------------|
| `register(email, password)` | Call `/auth/register`, store user |
| `login(email, password)` | Call `/auth/login`, store JWT |
| `logout()` | Clear token, reset all stores |

### PortfolioStore (MobX)

| Method | Description |
|--------|-------------|
| `fetchAnalytics()` | Call `GET /portfolio/analytics`, populate analytics |
| `refresh()` | Force reload of analytics |

### OrderStore (MobX)

| Method | Description |
|--------|-------------|
| `placeOrder(request)` | Call `POST /orders`, append to orders list |
| `fetchOrders(filter?)` | Load paginated order history |
| `cancelOrder(orderId)` | Call cancel endpoint, update local status |

### NotificationStore (MobX)

| Method | Description |
|--------|-------------|
| `fetchNotifications()` | Load notifications |
| `markRead(id)` | Call `PATCH /notifications/:id/read` |
| `get unreadCount()` | Computed — count of `isRead === false` |

### AccountStore (MobX)

| Method | Description |
|--------|-------------|
| `fetchAccounts()` | Load connected accounts |
| `connectAccount(request)` | Call `POST /accounts/connect` |
| `disconnectAccount(id)` | Call `DELETE /accounts/:id` |
| `syncAccount(id)` | Call `POST /sync/accounts/:id` |

---

## Repository Methods

### UserRepository

```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

### AccountRepository

```java
List<InvestmentAccount> findByUserIdAndDeletedAtIsNull(UUID userId);
Optional<InvestmentAccount> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
```

### AssetRepository

```java
List<Asset> findByPortfolioId(UUID portfolioId);
Optional<Asset> findByPortfolioIdAndTicker(UUID portfolioId, String ticker);
```

### OrderRepository

```java
Page<TradeOrder> findByAccountUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);
Optional<TradeOrder> findByIdAndAccountUserId(UUID id, UUID userId);
```

### TransactionRepository

```java
List<Transaction> findByAccountIdAndOccurredAtBetween(UUID accountId, OffsetDateTime from, OffsetDateTime to);
```

### NotificationRepository

```java
List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
long countByUserIdAndIsReadFalse(UUID userId);
```
