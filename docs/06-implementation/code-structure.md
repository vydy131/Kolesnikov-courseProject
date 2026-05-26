# Структура кода по слоям PCMEF

## Backend (Java 17 / Spring Boot 3.5)

### Control (controller/)
| Файл | Ответственность |
|---|---|
| AuthController.java | POST /auth/register, POST /auth/login |
| AccountController.java | Подключение/отключение брокерских счетов |
| PortfolioController.java | GET /portfolio/analytics |
| OrderController.java | Создание/отмена торговых ордеров |
| SyncController.java | POST /sync/accounts/{id} |
| NotificationController.java | Список и прочтение уведомлений |
| ReportController.java | Генерация и загрузка отчётов |

### Mediator (service/)
| Файл | Ответственность |
|---|---|
| UserService.java | Регистрация, BCrypt, создание Portfolio |
| AuthService.java | Проверка пароля, генерация JWT |
| AccountService.java | Подключение (AES-encrypt), отключение (soft delete) |
| AnalyticsService.java | P&L расчёт через MarketClient |
| OrderService.java | Ордер + транзакция + уведомление |
| SyncService.java | Синхронизация позиций от брокера |
| SyncScheduler.java | @Scheduled авто-синхронизация (15 мин) |
| ReportService.java | @Async генерация PDF/CSV |
| NotificationService.java | Создание и управление уведомлениями |

### Entity (entity/)
| Файл | Связи |
|---|---|
| User.java | 1:N Account, 1:1 Portfolio, 1:N Notification |
| Broker.java | Справочник, referenced by Account |
| InvestmentAccount.java | N:1 User, N:1 Broker, 1:N Order, 1:N Transaction |
| Portfolio.java | 1:1 User, 1:N Asset |
| Asset.java | N:1 Portfolio |
| TradeOrder.java | N:1 Account |
| Transaction.java | N:1 Account, N:1 Order (nullable) |
| MarketData.java | Standalone |
| Notification.java | N:1 User |
| Report.java | N:1 User, N:1 Portfolio |

### Foundation (repository/ + client/)
| Файл | Ключевые методы |
|---|---|
| UserRepository.java | findByEmailAndDeletedAtIsNull, existsByEmail |
| BrokerRepository.java | findByIsActiveTrue |
| InvestmentAccountRepository.java | findByUserIdAndDeletedAtIsNull, existsBy...AndDeletedAtIsNull |
| PortfolioRepository.java | findByUserId |
| AssetRepository.java | findByPortfolioId |
| TradeOrderRepository.java | findByAccountUserIdAndDeletedAtIsNull (Page) |
| TransactionRepository.java | findByAccountId |
| NotificationRepository.java | findByUserIdOrderByCreatedAtDesc |
| ReportRepository.java | findByUserId |
| MarketDataRepository.java | findByTicker |
| BrokerClient.java | validateToken, fetchPositions, fetchTransactions |
| MarketClient.java | getPrices(tickers) |

---

## Mobile (React Native / TypeScript / MobX)

### Presentation (screens/)
| Файл | Описание |
|---|---|
| LoginScreen.tsx | Вход (email + пароль) |
| RegisterScreen.tsx | Регистрация |
| PortfolioScreen.tsx | Аналитика, P&L, AssetCard, pull-to-refresh |
| AccountListScreen.tsx | Счета, Sync/Disconnect, FAB |
| ConnectBrokerScreen.tsx | Выбор брокера (chips), ввод токена |
| OrderListScreen.tsx | Ордера, статусы, infinite scroll, отмена |
| TradeOrderForm.tsx | BUY/SELL, тикер, кол-во, цена, live total |
| NotificationScreen.tsx | Уведомления, mark-as-read |

### State (stores/)
| Файл | Описание |
|---|---|
| userStore.ts | Авторизация, JWT в AsyncStorage, isAuthenticated |
| portfolioStore.ts | Загрузка аналитики |
| accountStore.ts | Счета + брокеры, connect/disconnect/sync |
| orderStore.ts | Ордера с пагинацией, placeOrder, cancelOrder |
| notificationStore.ts | Уведомления, unreadCount (computed), markRead |

### API Client (api/)
| Файл | Описание |
|---|---|
| apiClient.ts | Axios + Bearer interceptor + 401 logout |
| authApi.ts | register, login |
| portfolioApi.ts | getAnalytics |
| accountApi.ts | getAccounts, getBrokers, connect, disconnect, sync |
| orderApi.ts | getOrders (Page), placeOrder, cancelOrder |
| notificationApi.ts | getNotifications, markRead |
| reportApi.ts | generate, getReport, listReports |
