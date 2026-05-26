# Тестирование

## Общий результат

**40 тестов, 0 ошибок (100% success rate)**

Покрытие кода: JaCoCo (`./gradlew test jacocoTestReport`)

---

## Модульные тесты (JUnit 5 + Mockito)

### UserServiceTest (3 теста)
- `createUser_success` — создание пользователя, BCrypt-хэширование, создание портфеля
- `createUser_passwordEncoded` — проверка вызова passwordEncoder.encode()
- `createUser_duplicateEmail` — ConflictException при существующем email

### AuthServiceTest (3 теста)
- `login_validCredentials` — проверка пароля, генерация JWT
- `login_wrongPassword` — AppException UNAUTHORIZED
- `login_emailNotFound` — EntityNotFoundException

### AccountServiceTest (6 тестов)
- `connectBrokerAccount_success` — валидация токена, AES-шифрование, сохранение
- `connectBrokerAccount_duplicate` — ConflictException
- `connectBrokerAccount_invalidToken` — AppException INVALID_TOKEN
- `connectBrokerAccount_brokerNotFound` — EntityNotFoundException
- `disconnectAccount_notOwner` — ForbiddenException
- `disconnectAccount_success` — soft delete (deleted_at + REVOKED)

### AnalyticsServiceTest (4 теста)
- `buildAnalytics_emptyPortfolio` — нулевые значения
- `buildAnalytics_withAssets` — P&L: 10 шт * (300 - 250) = +500 (+20%)
- `buildAnalytics_priceNotInMarket` — fallback на avg_price, P&L = 0
- `buildAnalytics_portfolioNotFound` — EntityNotFoundException

### OrderServiceTest (2 теста)
- `createOrder_success` — ордер + транзакция + уведомление
- `createOrder_forbiddenAccount` — ForbiddenException

---

## Интеграционные тесты (@DataJpaTest)

### UserRepositoryTest (2 теста)
- `findByEmail_andExistsByEmail` — поиск с учётом soft delete
- `existsByEmail_softDeleted` — deleted_at != null не находится

---

## Контроллерные тесты (@WebMvcTest)

### AuthControllerTest (6 тестов)
- `register_validRequest_returns201`
- `register_duplicateEmail_returns409`
- `register_invalidEmail_returns400`
- `register_shortPassword_returns400`
- `login_validCredentials_returnsToken`
- `login_missingEmail_returns400`

### AccountControllerTest (5 тестов)
- `connect_validRequest_returns201`
- `connect_duplicate_returns409`
- `connect_missingBrokerId_returns400`
- `list_authenticated_returnsAccounts`
- `list_unauthenticated_returns401`

### OrderControllerTest (5 тестов)
- `create_validRequest_returns201`
- `create_missingTicker_returns400`
- `create_negativeQty_returns400`
- `list_returnsPaginatedOrders`
- `list_unauthenticated_returns401`

---

## Сводная таблица

| Тип теста | Количество | Файлов |
|---|---|---|
| Модульные (service) | 18 | 5 |
| Интеграционные (repository) | 2 | 1 |
| Контроллерные (@WebMvcTest) | 16 | 3 |
| Контекстные (ApplicationTests) | 1 | 1 |
| **Итого** | **40** | **10** |
