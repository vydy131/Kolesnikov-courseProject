# Sequence Flows

## Invariant Rule

Every flow must:
1. **Start** at Presentation (mobile screen or scheduled trigger)
2. **Pass through** Control (REST controller)
3. **Execute business logic** in Mediator (service)
4. **Reach** Foundation (repository / external client) only if data access required
5. **Never violate** layer direction — no back-references

---

## SF-01 — User Registration

```
User           Presentation        API Client          Control              Mediator             Foundation
 │                 │                    │                  │                    │                    │
 │──submit form──► │                    │                  │                    │                    │
 │                 │──POST /auth/reg──► │                  │                    │                    │
 │                 │                    │──HTTP request──► │                    │                    │
 │                 │                    │                  │──createUser()────► │                    │
 │                 │                    │                  │                    │──existsByEmail()──► │
 │                 │                    │                  │                    │ ◄──false───────────│
 │                 │                    │                  │                    │──bcrypt(password)   │
 │                 │                    │                  │                    │──save(user)───────► │
 │                 │                    │                  │                    │ ◄──User saved──────│
 │                 │                    │                  │ ◄──UserResponse───│                    │
 │                 │                    │ ◄──201 Created─── │                  │                    │
 │                 │ ◄──success─────────│                  │                    │                    │
 │ ◄──navigate──── │                    │                  │                    │                    │
```

---

## SF-02 — Login + JWT Issuance

```
User           API Client          Control (AuthController)   Mediator (AuthService)   Foundation
 │                │                         │                          │                    │
 │──POST login──► │                         │                          │                    │
 │                │──HTTP────────────────►  │                          │                    │
 │                │                         │──authenticate()────────► │                    │
 │                │                         │                          │──findByEmail()───► │
 │                │                         │                          │ ◄──User───────────│
 │                │                         │                          │──verifyPassword()  │
 │                │                         │                          │──generateJWT()     │
 │                │                         │ ◄──TokenResponse────────│                    │
 │                │ ◄──200 + JWT──────────── │                         │                    │
 │──store token──►│                          │                         │                    │
```

---

## SF-03 — Connect Broker Account

```
User          Presentation       API Client        Control            Mediator             Foundation      BrokerClient
 │                │                  │                │                  │                    │                │
 │──enter token──►│                  │                │                  │                    │                │
 │                │──POST /connect──►│                │                  │                    │                │
 │                │                  │──HTTP────────► │                  │                    │                │
 │                │                  │                │──connectAccount─►│                    │                │
 │                │                  │                │                  │──validate token──────────────────► │
 │                │                  │                │                  │◄──OK──────────────────────────────│
 │                │                  │                │                  │──encrypt(token)    │                │
 │                │                  │                │                  │──save(account)───► │                │
 │                │                  │                │◄──AccountResponse│                    │                │
 │                │                  │◄──201──────────│                  │                    │                │
 │◄──success──────│                  │                │                  │                    │                │
```

---

## SF-04 — Portfolio Analytics

```
PortfolioStore    API Client         Control              Mediator (AnalyticsService)   Foundation   MarketClient
     │                │                  │                          │                       │              │
     │──fetchAnalytics│                  │                          │                       │              │
     │                │──GET /analytics─►│                          │                       │              │
     │                │                  │──buildAnalytics()───────►│                       │              │
     │                │                  │                          │──findAssets()─────────►│             │
     │                │                  │                          │◄──[Asset list]────────│              │
     │                │                  │                          │──getPrices(tickers)────────────────►│
     │                │                  │                          │◄──{ticker: price}─────────────────── │
     │                │                  │                          │──calculate totals                    │
     │                │                  │◄──PortfolioResponse─────│                       │              │
     │                │◄──200────────────│                          │                       │              │
     │◄──analytics data│                 │                          │                       │              │
```

---

## SF-05 — Place Trade Order

```
TradeForm      API Client      Control (OrderController)   Mediator (OrderService)   Foundation   BrokerClient
    │               │                    │                          │                    │              │
    │──submit───────►│                   │                          │                    │              │
    │               │──POST /orders─────►│                         │                    │              │
    │               │                    │──createOrder()──────────►│                   │              │
    │               │                    │                          │──findAccount()────►│              │
    │               │                    │                          │◄──Account─────────│              │
    │               │                    │                          │──[validate owner]  │              │
    │               │                    │                          │──submitOrder()─────────────────►  │
    │               │                    │                          │◄──BrokerOrderId───────────────── │
    │               │                    │                          │──saveOrder()──────►│              │
    │               │                    │                          │──saveTransaction()─►│             │
    │               │                    │                          │──notifyUser()      │              │
    │               │                    │◄──TradeOrderResponse────│                    │              │
    │               │◄──201──────────────│                          │                   │              │
    │◄──order placed─│                   │                          │                   │              │
```

---

## SF-06 — Broker Sync (Scheduled)

```
Scheduler    Control (SyncController)   Mediator (SyncService)   Foundation   BrokerClient
    │                  │                         │                    │              │
    │──POST /sync──────►│                         │                  │              │
    │                  │──syncBrokerAccount()────►│                  │              │
    │                  │                          │──findAccount()──► │             │
    │                  │                          │◄──Account────────│              │
    │                  │                          │──fetchPositions()──────────────►│
    │                  │                          │◄──[Positions]──────────────────│
    │                  │                          │──fetchTransactions()───────────►│
    │                  │                          │◄──[Transactions]───────────────│
    │                  │                          │──reconcile()       │            │
    │                  │                          │──saveAll(assets)──►│            │
    │                  │                          │──saveAll(txns)────►│            │
    │                  │                          │──updateSyncedAt()─►│            │
    │                  │◄──202 Accepted──────────│                    │            │
```

---

## Layer Violation Examples (FORBIDDEN)

| Forbidden Call                           | Correct Alternative                         |
|------------------------------------------|---------------------------------------------|
| Controller → Repository directly         | Controller → Service → Repository           |
| Entity imports Service                   | Service reads Entity, not vice versa        |
| Component calls Axios directly           | Component → Store → API Client → Axios      |
| Service returns Entity to Controller     | Service maps Entity → DTO, returns DTO      |
| Repository contains business logic       | Move logic to Service layer                 |
