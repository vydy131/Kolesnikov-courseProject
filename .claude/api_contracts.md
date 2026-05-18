# API Contracts

## Conventions

- Base URL: `/api/v1`
- Format: `application/json`
- Authentication: `Authorization: Bearer <JWT>` (except `/auth/*`)
- Error format: `{ "error": "message", "code": "ERROR_CODE" }`
- All list endpoints support: `?page=0&size=20&sort=createdAt,desc`
- OpenAPI/Swagger available at `/swagger-ui.html`

---

## Authentication — `/auth`

### POST `/auth/register`

Register a new user.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "createdAt": "2024-01-01T10:00:00Z"
}
```

**Errors:** `409 Conflict` — email already registered

---

### POST `/auth/login`

Authenticate and receive JWT.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Errors:** `401 Unauthorized` — invalid credentials

---

## Investment Accounts — `/accounts`

### POST `/accounts/connect`

Connect a broker account.

**Request:**
```json
{
  "brokerId": "uuid",
  "accountNumber": "12345678",
  "brokerToken": "raw-api-token"
}
```

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "brokerId": "uuid",
  "brokerName": "Tinkoff",
  "accountNumber": "12345678",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00Z"
}
```

**Errors:** `409 Conflict` — account already connected; `400 Bad Request` — invalid token

---

### GET `/accounts`

List user's connected accounts.

**Response `200 OK`:**
```json
[
  {
    "id": "uuid",
    "brokerName": "Tinkoff",
    "accountNumber": "12345678",
    "status": "ACTIVE",
    "syncedAt": "2024-01-01T09:00:00Z"
  }
]
```

---

### DELETE `/accounts/{accountId}`

Disconnect (soft-delete) a broker account.

**Response `204 No Content`**

---

## Portfolio — `/portfolio`

### GET `/portfolio/analytics`

Get portfolio analytics for the current user.

**Response `200 OK`:**
```json
{
  "totalValue": 125000.50,
  "currency": "RUB",
  "profitLoss": 5000.00,
  "profitLossPercent": 4.17,
  "assets": [
    {
      "ticker": "SBER",
      "name": "Сбербанк",
      "qty": 100,
      "avgPrice": 280.00,
      "currentPrice": 295.00,
      "totalValue": 29500.00,
      "profitLoss": 1500.00,
      "profitLossPercent": 5.36
    }
  ],
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

---

## Trade Orders — `/orders`

### POST `/orders`

Place a new trade order.

**Request:**
```json
{
  "accountId": "uuid",
  "ticker": "SBER",
  "direction": "BUY",
  "qty": 10,
  "price": 295.00
}
```

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "ticker": "SBER",
  "direction": "BUY",
  "qty": 10,
  "price": 295.00,
  "status": "PENDING",
  "placedAt": "2024-01-01T10:00:00Z"
}
```

**Errors:** `400 Bad Request` — invalid qty/price; `422 Unprocessable` — insufficient balance

---

### GET `/orders`

List orders for the current user. Supports `?accountId=uuid&status=PENDING`.

**Response `200 OK` (paginated):**
```json
{
  "content": [ { ...order... } ],
  "page": 0,
  "size": 20,
  "totalElements": 45
}
```

---

## Reports — `/reports`

### POST `/reports/generate`

Generate a portfolio report.

**Request:**
```json
{
  "type": "PERFORMANCE",
  "format": "PDF",
  "periodFrom": "2024-01-01",
  "periodTo": "2024-12-31"
}
```

**Response `202 Accepted`:**
```json
{
  "reportId": "uuid",
  "status": "GENERATING",
  "estimatedReadyAt": "2024-01-01T10:01:00Z"
}
```

---

### GET `/reports/{reportId}`

Get report status or download link.

**Response `200 OK`:**
```json
{
  "id": "uuid",
  "type": "PERFORMANCE",
  "format": "PDF",
  "status": "READY",
  "downloadUrl": "/reports/uuid/download",
  "generatedAt": "2024-01-01T10:01:30Z"
}
```

---

## Notifications — `/notifications`

### GET `/notifications`

List notifications for the current user.

**Response `200 OK`:**
```json
[
  {
    "id": "uuid",
    "type": "ORDER_FILLED",
    "title": "Order executed",
    "body": "Your BUY order for SBER x10 has been filled at 295.00",
    "isRead": false,
    "createdAt": "2024-01-01T10:00:00Z"
  }
]
```

---

### PATCH `/notifications/{notificationId}/read`

Mark notification as read.

**Response `200 OK`:**
```json
{ "id": "uuid", "isRead": true }
```

---

## Broker Sync — `/sync`

### POST `/sync/accounts/{accountId}`

Trigger manual sync of a broker account.

**Response `202 Accepted`:**
```json
{ "accountId": "uuid", "status": "SYNCING", "startedAt": "2024-01-01T10:00:00Z" }
```

---

## Error Codes

| HTTP Status | Code                | Meaning                              |
|-------------|---------------------|--------------------------------------|
| 400         | VALIDATION_ERROR    | Invalid request body or params       |
| 401         | UNAUTHORIZED        | Missing or invalid JWT               |
| 403         | FORBIDDEN           | Authenticated but not authorized     |
| 404         | NOT_FOUND           | Resource does not exist              |
| 409         | CONFLICT            | Duplicate resource (email, account)  |
| 422         | BUSINESS_RULE_ERROR | Rule violation (insufficient funds)  |
| 500         | INTERNAL_ERROR      | Unexpected server error              |
