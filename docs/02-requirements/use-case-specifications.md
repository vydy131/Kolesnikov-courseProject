# Спецификации прецедентов

## UC-001: Регистрация пользователя

**Акторы:** Инвестор (инициатор), Система (исполнитель)

**Предусловия:** Пользователь не авторизован, email не зарегистрирован.

**Основной поток:**
1. Пользователь выбирает «Регистрация»
2. Система отображает форму (email, пароль)
3. Пользователь заполняет и нажимает «Зарегистрироваться»
4. Система проверяет email, сложность пароля, уникальность
5. Система создает пользователя (BCrypt hash), генерирует UUID
6. Система создает пустой Portfolio
7. Система возвращает 201 Created с UserResponse

**Исключения:**
- E1: Email уже используется -> 409 Conflict
- E2: Некорректный пароль -> 400 Bad Request

**PCMEF-поток:** `Presentation -> API Client -> Control (AuthController.register) -> Mediator (UserService.createUser) -> Foundation (UserRepository.save)`

---

## UC-002: Подключение брокерского счёта

**Акторы:** Инвестор, Брокер API, Система

**Предусловия:** Пользователь авторизован, брокер поддерживается.

**Основной поток:**
1. Пользователь выбирает брокера и вводит API-токен
2. Система отправляет токен на валидацию в BrokerClient
3. Брокер подтверждает доступ
4. Система шифрует токен (AES-256-GCM)
5. Система сохраняет InvestmentAccount (статус ACTIVE)
6. Возврат 201 Created

**Исключения:**
- E1: Неверный токен -> 400 Bad Request
- E2: Счёт уже подключен -> 409 Conflict
- E3: Брокер недоступен -> 503 Service Unavailable

---

## UC-003: Просмотр аналитики портфеля

**Акторы:** Инвестор, Система

**Предусловия:** Подключен минимум один счёт, данные синхронизированы.

**Основной поток:**
1. Пользователь открывает PortfolioScreen
2. PortfolioStore.fetchAnalytics() -> GET /portfolio/analytics
3. AnalyticsService загружает активы из AssetRepository
4. MarketClient.getPrices() получает текущие котировки
5. Рассчитывается totalValue, P&L, profitLossPercent по каждому активу
6. Возврат PortfolioAnalyticsResponse

---

## UC-004: Создание торговой заявки

**Акторы:** Инвестор, Брокер API, Система

**Предусловия:** Пользователь авторизован, счёт подключен, брокер доступен.

**Основной поток:**
1. Пользователь заполняет TradeOrderForm (тикер, BUY/SELL, кол-во, цена)
2. OrderService проверяет владение счётом
3. BrokerClient.submitOrder() отправляет заявку брокеру
4. Система сохраняет TradeOrder (статус PENDING) и Transaction
5. NotificationService отправляет уведомление ORDER_PLACED
6. Возврат 201 Created

---

## UC-005: Синхронизация данных брокера

**Триггер:** SyncScheduler (каждые 15 мин) или POST /sync/accounts/{id}

**Поток:**
1. SyncService загружает позиции и транзакции через BrokerClient
2. Reconcile с локальными данными
3. AssetRepository.saveAll(), TransactionRepository.saveAll()
4. Обновляется syncedAt
5. При ошибке — Notification с типом SYNC_ERROR

---

## UC-006: Генерация отчёта

**Поток:**
1. POST /reports/generate (type, format, period)
2. ReportService (@Async) загружает данные, рассчитывает метрики
3. Генерирует PDF/CSV, сохраняет Report (статус GENERATING -> READY)
4. Возврат 202 Accepted

---

## UC-007: Уведомления

**Триггер:** Бизнес-событие (ордер исполнен, ошибка синхронизации, ценовой алерт)

**Поток:**
1. NotificationService.send(userId, type, title, body)
2. NotificationRepository.save()
3. GET /notifications -> список для пользователя
4. PATCH /notifications/{id}/read -> отметка о прочтении
