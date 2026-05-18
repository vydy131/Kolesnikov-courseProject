# Coding Guidelines

## General Principles

- PCMEF layer direction is **strictly enforced** — no reverse dependencies
- Thin controllers, fat services — all orchestration belongs in the Mediator (Service) layer
- DTOs are mandatory at the API boundary — never expose JPA Entities directly
- All external calls (broker APIs, market data) must go through dedicated client wrappers

---

## Backend (Java / Spring Boot)

### Package Structure

```
com.investagg
  ├── controller/          # Control layer — @RestController classes
  ├── service/             # Mediator layer — @Service classes
  ├── repository/          # Foundation layer — @Repository interfaces
  ├── entity/              # Entity layer — @Entity JPA classes
  ├── dto/
  │   ├── request/         # Inbound DTOs (RegisterRequest, TradeOrderRequest)
  │   └── response/        # Outbound DTOs (UserResponse, PortfolioResponse)
  ├── mapper/              # DTO ↔ Entity converters
  ├── client/              # External API clients (BrokerClient, MarketClient)
  ├── security/            # JWT filter, SecurityConfig
  ├── exception/           # Custom exceptions + GlobalExceptionHandler
  └── config/              # Spring configs (CORS, OpenAPI, etc.)
```

### Controller Rules

```java
// CORRECT
@PostMapping("/orders")
@ResponseStatus(HttpStatus.CREATED)
public TradeOrderResponse createOrder(@Valid @RequestBody TradeOrderRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
    return orderService.createOrder(request, userDetails.getUsername());
}

// WRONG — Controller contains business logic
@PostMapping("/orders")
public TradeOrderResponse createOrder(@RequestBody TradeOrderRequest request) {
    if (request.getQty() <= 0) throw new BadRequestException("..."); // validation belongs in @Valid
    Account acc = accountRepository.findById(request.getAccountId()); // direct repo access forbidden
    ...
}
```

- Use `@Valid` for all request body validation
- Use `@AuthenticationPrincipal` to extract current user — never parse JWT manually in controller
- Return DTOs, never Entities
- Use `@ResponseStatus` to set HTTP status codes explicitly

### Service Rules

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MarketClient marketClient;
    private final PortfolioMapper mapper;

    @Transactional
    public PortfolioResponse buildAnalytics(UUID userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("Portfolio not found for user " + userId));

        List<String> tickers = portfolio.getAssets().stream()
            .map(Asset::getTicker)
            .toList();

        Map<String, BigDecimal> prices = marketClient.getPrices(tickers);

        // business logic: calculate total value
        BigDecimal totalValue = portfolio.getAssets().stream()
            .map(a -> prices.getOrDefault(a.getTicker(), BigDecimal.ZERO).multiply(a.getQty()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return mapper.toResponse(portfolio, totalValue);
    }
}
```

- Mark class-level `@Transactional(readOnly = true)`, override with `@Transactional` on write methods
- Throw specific exceptions (`EntityNotFoundException`, `ConflictException`) — never raw `RuntimeException`
- Use `@RequiredArgsConstructor` with final fields instead of `@Autowired`

### Repository Rules

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.email = :email")
    Optional<User> findActiveByEmail(@Param("email") String email);
}
```

- Extend `JpaRepository<Entity, UUID>`
- Use Spring Data method naming where possible
- Use `@Query` only when method naming is insufficient
- Always filter `deleted_at IS NULL` for soft-deleted entities

### Entity Rules

```java
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InvestmentAccount> accounts = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
```

- `FetchType.LAZY` by default — NEVER use `EAGER` without justification
- `cascade = CascadeType.ALL` only for aggregate roots
- Always include `created_at`, `deleted_at` for domain entities
- No business logic inside Entity classes — getters/setters only

### Exception Handling

Define a `GlobalExceptionHandler`:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EntityNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }
    // ConflictException → 409, ValidationException → 400, etc.
}
```

Never let raw stack traces leak to the client.

### Security

- JWT token validated by a `OncePerRequestFilter` in the `security/` package
- Password hashing via `BCryptPasswordEncoder` — minimum strength 12
- Broker tokens must be encrypted at rest (AES-256)
- Never log: passwords, JWT tokens, broker API keys, sensitive financial data

---

## Frontend (React Native / TypeScript / MobX)

### Strict TypeScript

- `"strict": true` in `tsconfig.json` — no `any` allowed
- All API response shapes typed in `src/types/api.ts`
- Use `type` for data shapes, `interface` for contracts with implementations

### Component Rules

- Components are **dumb** — they render props and call store actions
- No `fetch`, `axios`, or `AsyncStorage` calls inside components
- No business logic or data transformations in JSX

```tsx
// CORRECT
const PortfolioScreen: React.FC = observer(() => {
  const { analytics, loading } = portfolioStore;
  useEffect(() => { portfolioStore.fetchAnalytics(); }, []);
  if (loading) return <LoadingSpinner />;
  return <PortfolioView data={analytics} />;
});

// WRONG — component doing API work
const PortfolioScreen = () => {
  const [data, setData] = useState(null);
  useEffect(() => {
    axios.get('/portfolio/analytics').then(r => setData(r.data)); // forbidden
  }, []);
};
```

### MobX Store Rules

- Use `makeAutoObservable(this)` in constructors
- Side effects (`runInAction`) required when mutating state inside `async` functions
- One store per domain: `UserStore`, `PortfolioStore`, `OrderStore`, `NotificationStore`
- Stores are singletons exported as instances

### API Client Rules

- Single `apiClient` (Axios instance) with interceptors for JWT header injection and token refresh
- Each domain has its own API module: `portfolioApi`, `orderApi`, `authApi`
- Handle errors in the store layer, not in the API module

---

## Common Rules (Both Sides)

- **No magic numbers** — use named constants
- **No commented-out code** — delete it
- **No TODO without a ticket** — link to an issue
- **Pagination mandatory** for any list endpoint returning potentially large data sets
- **Input validation** at API boundary (Spring `@Valid` / Zod on frontend)
