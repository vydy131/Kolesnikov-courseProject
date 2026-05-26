# Диаграммы последовательностей

## SF-01: Регистрация пользователя

```
User        Presentation     API Client       Control              Mediator              Foundation
 |              |                |                |                    |                      |
 |--submit---->|                |                |                    |                      |
 |              |--POST /auth-->|                |                    |                      |
 |              |                |--HTTP-------->|                    |                      |
 |              |                |                |--createUser()---->|                      |
 |              |                |                |                    |--existsByEmail()---->|
 |              |                |                |                    |<--false-------------|
 |              |                |                |                    |--bcrypt(password)    |
 |              |                |                |                    |--save(user)--------->|
 |              |                |                |                    |<--User saved---------|
 |              |                |                |<--UserResponse----|                      |
 |              |                |<--201 Created--|                    |                      |
 |              |<--success------|                |                    |                      |
 |<--navigate---|                |                |                    |                      |
```

**Классы:** AuthController.register() -> UserService.createUser() -> UserRepository.save()

---

## SF-02: Авторизация (JWT)

```
User        API Client       Control (Auth)       Mediator (Auth)       Foundation
 |              |                  |                     |                   |
 |--POST login->|                  |                     |                   |
 |              |--HTTP----------->|                     |                   |
 |              |                  |--authenticate()---->|                   |
 |              |                  |                     |--findByEmail()--->|
 |              |                  |                     |<--User-----------|
 |              |                  |                     |--verifyPassword() |
 |              |                  |                     |--generateJWT()    |
 |              |                  |<--TokenResponse-----|                   |
 |              |<--200 + JWT------|                     |                   |
 |--store token>|                  |                     |                   |
```

**Классы:** AuthController.login() -> AuthService.login() -> UserRepository.findByEmail(), JwtService.generateToken()

---

## SF-03: Подключение брокерского счёта

```
User     Presentation    API Client     Control          Mediator            Foundation    BrokerClient
 |           |               |             |                |                   |              |
 |--token--->|               |             |                |                   |              |
 |           |--POST-------->|             |                |                   |              |
 |           |               |--HTTP------>|                |                   |              |
 |           |               |             |--connect()---->|                   |              |
 |           |               |             |                |--validateToken()--------------->|
 |           |               |             |                |<--OK--------------------------------|
 |           |               |             |                |--encrypt(token)   |              |
 |           |               |             |                |--save(account)--->|              |
 |           |               |             |<--AccountResp--|                   |              |
 |           |               |<--201-------|                |                   |              |
 |<--success-|               |             |                |                   |              |
```

**Классы:** AccountController.connect() -> AccountService.connectBrokerAccount() -> BrokerClient.validateToken(), AesEncryptionService.encrypt(), InvestmentAccountRepository.save()

---

## SF-04: Аналитика портфеля

```
PortfolioStore   API Client     Control           Mediator (Analytics)    Foundation    MarketClient
     |               |             |                      |                  |              |
     |--fetch------->|             |                      |                  |              |
     |               |--GET------->|                      |                  |              |
     |               |             |--buildAnalytics()--->|                  |              |
     |               |             |                      |--findAssets()--->|              |
     |               |             |                      |<--[Assets]------|              |
     |               |             |                      |--getPrices()------------------>|
     |               |             |                      |<--{ticker:price}---------------|
     |               |             |                      |--calculate P&L   |              |
     |               |             |<--PortfolioResp------|                  |              |
     |               |<--200-------|                      |                  |              |
     |<--analytics---|             |                      |                  |              |
```

**Классы:** PortfolioController.getAnalytics() -> AnalyticsService.buildAnalytics() -> PortfolioRepository, AssetRepository, MarketClient.getPrices()

---

## SF-05: Создание торговой заявки

```
TradeForm    API Client     Control (Order)    Mediator (Order)     Foundation    BrokerClient
    |            |               |                   |                  |              |
    |--submit--->|               |                   |                  |              |
    |            |--POST-------->|                   |                  |              |
    |            |               |--createOrder()--->|                  |              |
    |            |               |                   |--findAccount()--->|             |
    |            |               |                   |<--Account--------|              |
    |            |               |                   |--validateOwner    |              |
    |            |               |                   |--submitOrder()------------------>|
    |            |               |                   |<--BrokerOrderId-----------------|
    |            |               |                   |--saveOrder()----->|              |
    |            |               |                   |--saveTxn()------->|              |
    |            |               |                   |--notifyUser()     |              |
    |            |               |<--OrderResponse---|                  |              |
    |            |<--201---------|                   |                  |              |
    |<--placed---|               |                   |                  |              |
```

**Классы:** OrderController.createOrder() -> OrderService.createOrder() -> BrokerClient.submitOrder(), TradeOrderRepository.save(), TransactionRepository.save(), NotificationService.send()

---

## SF-06: Синхронизация (Scheduled)

```
Scheduler    Control (Sync)    Mediator (Sync)     Foundation    BrokerClient
    |             |                  |                  |              |
    |--POST------>|                  |                  |              |
    |             |--syncAccount()-->|                  |              |
    |             |                  |--findAccount()--->|             |
    |             |                  |<--Account--------|              |
    |             |                  |--fetchPositions()-------------->|
    |             |                  |<--[Positions]-------------------|
    |             |                  |--fetchTransactions()----------->|
    |             |                  |<--[Transactions]----------------|
    |             |                  |--reconcile()      |             |
    |             |                  |--saveAll(assets)->|             |
    |             |                  |--saveAll(txns)--->|             |
    |             |                  |--updateSyncedAt->|             |
    |             |<--202-----------|                   |             |
```

**Классы:** SyncController.syncAccount() -> SyncService.syncBrokerAccount() -> BrokerClient, AssetRepository.saveAll(), TransactionRepository.saveAll()
