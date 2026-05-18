# Investment Aggregator Platform — Architecture

## Project Goal

Investment Aggregator Platform is a distributed fintech system that aggregates brokerage accounts from multiple providers into a single unified interface for analytics, reporting and trading operations.

The system follows PCMEF architecture adapted for mobile client-server applications.

---

# Technology Stack

## Frontend
- React Native
- TypeScript
- MobX
- React Navigation

## Backend
- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- JWT Security
- OpenAPI / Swagger
- Gradle

---

# Architectural Style

## Selected Pattern
Distributed PCMEF (client-server adaptation)

Client:
- Presentation
- State Management
- API Client
- Local Cache

Server:
- Control
- Mediator
- Entity
- Foundation

---

# Layer Responsibilities

## Presentation
Responsible for:
- UI rendering
- screen navigation
- displaying data
- user interaction

Must NOT:
- contain business logic
- directly access database
- perform calculations

---

## State Management
Responsible for:
- MobX stores
- reactive state
- screen state synchronization

Must NOT:
- contain backend orchestration logic
- directly call repositories

---

## API Client
Responsible for:
- REST communication
- DTO serialization
- authentication headers
- retry logic

Must NOT:
- contain business logic

---

## Control Layer
Responsible for:
- REST endpoints
- request validation
- DTO mapping
- authentication entry points

Must NOT:
- contain complex business logic
- directly access database

---

## Mediator Layer
Responsible for:
- orchestration
- business rules
- analytics calculations
- broker integrations
- transaction workflows

This is the main business logic layer.

---

## Entity Layer
Responsible for:
- domain model
- business entities
- persistence mappings

Entities must remain persistence-focused and lightweight.

---

## Foundation Layer
Responsible for:
- repositories
- database access
- external API clients
- infrastructure integrations

Must NOT:
- contain business rules

---

# Dependency Rules

STRICT dependency direction:

Presentation
→ State Management
→ API Client
→ Control
→ Mediator
→ Entity
→ Foundation

Reverse dependencies are forbidden.

Examples of forbidden calls:
- Entity → Service
- Repository → Controller
- UI → Database
- Controller → Repository (directly)

---

# Database Rules

- PostgreSQL only
- UUID primary keys
- Normalization up to 3NF
- Foreign keys mandatory
- Indexes required for JOIN and WHERE fields
- DTO layer mandatory between API and Entity

Soft delete:
- implemented via deleted_at
- physical delete avoided for critical financial data

---

# API Rules

- REST only
- JSON payloads
- JWT authentication
- DTO-based contracts
- OpenAPI documentation required

All endpoints must:
- validate input
- return structured errors
- avoid leaking internal entities

---

# Coding Standards

## Backend
- Controllers are thin
- Services contain orchestration
- Repositories only access data
- Business rules belong to Mediator layer

## Frontend
- UI components must remain dumb
- State belongs to MobX stores
- API access only through dedicated clients

---

# Performance Rules

- Lazy loading by default
- Avoid N+1 queries
- Pagination mandatory for large collections
- External API calls must support timeout/retry

---

# Security Rules

- JWT authentication required
- Password hashing via BCrypt
- Sensitive data must not appear in logs
- Broker tokens encrypted at rest

---

# Testing Rules

Mandatory:
- unit tests for services
- repository integration tests
- API endpoint tests
- edge-case validation

Mock required for:
- broker APIs
- market data providers

---

# Main Domain Entities

Core entities:
- User
- Broker
- InvestmentAccount
- Portfolio
- Asset
- TradeOrder
- Transaction
- Notification
- Report
- MarketData

---

# Primary Use Cases

- User registration
- Broker account connection
- Portfolio analytics
- Trade order execution
- Report generation
- Notifications
- Broker synchronization

---

# Architectural Goal

The primary goal of the architecture is:

- maintainability
- scalability
- strict separation of concerns
- clean business orchestration
- safe financial operations
- future microservice readiness
