# Investment Aggregator Platform

Fintech system that aggregates multiple brokerage accounts into a single unified interface for portfolio analytics, trade execution, and reporting.

---

## Project Structure

```
courseProject/
  ├── backend/                  # Java 17 / Spring Boot
  │   ├── src/main/java/com/investagg/
  │   │   ├── controller/       # Control layer — REST endpoints
  │   │   ├── service/          # Mediator layer — business logic
  │   │   ├── repository/       # Foundation layer — data access
  │   │   ├── entity/           # Entity layer — JPA domain objects
  │   │   ├── dto/
  │   │   │   ├── request/      # Inbound DTOs
  │   │   │   └── response/     # Outbound DTOs
  │   │   ├── client/           # External API clients (broker, market data)
  │   │   ├── mapper/           # DTO ↔ Entity mappers
  │   │   ├── security/         # JWT filter, SecurityConfig
  │   │   ├── exception/        # Custom exceptions + GlobalExceptionHandler
  │   │   └── config/           # Spring configs
  │   ├── src/main/resources/
  │   │   ├── application.yml
  │   │   └── db/migration/     # Flyway SQL migrations
  │   └── build.gradle
  │
  └── mobile/                   # React Native / TypeScript / MobX
      └── src/
          ├── screens/          # Presentation layer
          ├── components/       # Reusable UI components
          ├── stores/           # MobX state management
          ├── api/              # API client wrappers (Axios)
          ├── navigation/       # React Navigation config
          ├── types/            # TypeScript interfaces
          └── cache/            # Local cache utilities
```

---

## Technology Stack

| Layer    | Technology                                          |
|----------|-----------------------------------------------------|
| Backend  | Java 17, Spring Boot 3, Spring Data JPA, Gradle     |
| Database | PostgreSQL, Flyway migrations                       |
| Security | Spring Security, JWT (RS256)                        |
| API Docs | OpenAPI 3 / Swagger UI                              |
| Mobile   | React Native, TypeScript, MobX, React Navigation   |
| HTTP     | Axios (mobile client)                               |

---

## Build & Run Commands

### Backend

```bash
# Build
./gradlew build

# Run (dev)
./gradlew bootRun

# Run tests
./gradlew test

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Check for dependency vulnerabilities
./gradlew dependencyCheckAnalyze
```

### Mobile

```bash
# Install dependencies
npm install

# Run on iOS simulator
npx react-native run-ios

# Run on Android emulator
npx react-native run-android

# Run tests
npm test

# Type check
npx tsc --noEmit

# Lint
npm run lint
```

---

## Architecture — PCMEF

This project follows a **distributed PCMEF architecture** (client-server).

### Dependency Direction (strictly enforced — never reversed)

```
[Mobile Client]                    [Backend Server]
Presentation                       Control (REST)
  → State (MobX)      ──HTTP──►      → Mediator (Services)
    → API Client                         → Entity (JPA)
                                           → Foundation (Repositories + Clients)
```

### Layer Rules

| Layer | Location | Responsibility | Must NOT |
|-------|----------|----------------|----------|
| Presentation | `screens/`, `components/` | Render UI, handle user events | Contain business logic, call API directly |
| State | `stores/` | MobX reactive state, delegate to API client | Call repositories, orchestrate backend |
| API Client | `api/` | HTTP calls, auth headers, error parsing | Contain business logic |
| Control | `controller/` | Accept HTTP request, validate, map DTO, delegate | Call Repository directly, contain business logic |
| Mediator | `service/` | Business rules, orchestration, calculations | Return raw Entities, access DB directly |
| Entity | `entity/` | JPA persistence mapping | Import or call Services |
| Foundation | `repository/`, `client/` | DB access, external API calls | Contain business rules |

---

## Key Conventions

### Backend

- **Controllers** are thin: `@Valid` → delegate to Service → return DTO
- **Services** own all business logic; annotated `@Transactional(readOnly = true)` at class level
- **Entities** use `FetchType.LAZY` for all relationships; never `EAGER`
- **DTOs** mandatory at the API boundary — never expose `@Entity` objects in responses
- **UUID** primary keys everywhere: `@GeneratedValue(strategy = GenerationType.UUID)`
- **Soft delete** via `deleted_at` for financial entities; physical DELETE forbidden for `users`, `investment_accounts`, `trade_orders`, `transactions`
- **Exceptions**: throw `EntityNotFoundException` (→ 404), `ConflictException` (→ 409), `ForbiddenException` (→ 403) — caught by `GlobalExceptionHandler`
- **Security**: BCrypt strength ≥ 12; broker tokens AES-256 encrypted at rest; never log passwords or tokens

### Database

- All tables: `snake_case`, UUID PK with `DEFAULT gen_random_uuid()`
- Timestamps: `TIMESTAMPTZ` (UTC)
- FK constraints named: `fk_{table}_{ref}`
- Indexes required for all FK columns and common `WHERE`/`ORDER BY` fields
- Migrations: `V{n}__{description}.sql` (Flyway format)

### Mobile

- Components are dumb — receive props, dispatch store actions only
- All data access: Component → Store → API Client → Backend
- No `axios`/`fetch` calls inside components or `useEffect` hooks
- `makeAutoObservable(this)` in all store constructors
- `runInAction()` required for async state mutations
- Strict TypeScript — no `any`

---

## Testing

### Backend

- **Service tests** (JUnit 5 + Mockito): highest priority, all business logic paths
- **Repository tests** (`@DataJpaTest`): verify queries against test DB
- **Controller tests** (`@WebMvcTest`): verify HTTP contract, validation, status codes
- Always mock: `BrokerClient`, `MarketClient`, email/notification delivery
- Always test: null inputs, duplicate creation, unauthorized access, invalid UUIDs

### Mobile

- **Store tests** (Jest): verify state transitions and API delegation
- Mock all API modules with `jest.fn()`
- Test error state handling (network failure, 4xx responses)

---

## Custom Slash Commands

| Command | Usage |
|---------|-------|
| `/new-entity <Name>` | Scaffold a JPA entity + repository + migration |
| `/new-service <Name>` | Scaffold a service + DTOs + controller stub |
| `/new-migration <description>` | Create the next Flyway migration file |
| `/check-pcmef <file or path>` | Audit code for PCMEF layer violations |

---

## Sub-Agents

Specialized agents are available in `.claude/agents/`:

| Agent | When to use |
|-------|-------------|
| **System Architect** | Designing services, controllers, DTOs, API contracts |
| **Database Administrator** | Schema design, DDL, Flyway migrations, indexes |
| **Frontend Developer** | React Native screens, MobX stores, API client wrappers |
| **QA Engineer** | Writing tests, code review, PCMEF compliance checks |

---

## Implementation Plan

`.claude/implementation_plan.md` — 19-фазовый план реализации с конкретными артефактами, таблицей статусов и графом зависимостей между фазами.

---

## Context Files

All project reference documents live in `.claude/`:

| File | Contents |
|------|----------|
| `architecture.md` | Full architectural specification |
| `pcmef_layers.md` | Layer responsibilities and rules |
| `domain_model.md` | Entities and relationships |
| `database_model.md` | Full PostgreSQL schema with DDL |
| `api_contracts.md` | REST API endpoints and DTO shapes |
| `use_cases.md` | Functional requirements (UC-001 through UC-007) |
| `sequence_flows.md` | Sequence diagrams for each use case |
| `methods_catalog.md` | All key service, repository, and store methods |
| `coding_guidelines.md` | Backend and frontend coding standards |
| `patterns.md` | Design patterns in use |
| `orm_strategy.md` | JPA mapping strategy |

---

## Security Constraints

- JWT required on all endpoints except `/auth/register` and `/auth/login`
- Broker API tokens encrypted at rest (AES-256), never returned in API responses
- Passwords: BCrypt, never logged or serialized
- All inputs validated at API boundary (`@Valid` + BindingResult or `@ExceptionHandler`)
- Pagination mandatory for all list endpoints
- Structured error responses only — no stack traces to client
