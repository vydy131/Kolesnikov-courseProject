# 07. Пользовательский интерфейс

## Обзор

Мобильное приложение реализовано на React Native 0.85 / TypeScript / MobX 6 с React Navigation 7. Включает 8 экранов, 3 переиспользуемых компонента, 2 навигационных стека.

## Навигация

### RootNavigator (observer)
Переключает между AuthStack и MainTabs на основе `userStore.isAuthenticated`.
При запуске восстанавливает сессию из AsyncStorage.

### AuthStack
- **LoginScreen** — email + пароль, "Sign In", ссылка на Register
- **RegisterScreen** — email + пароль (min 8 символов)

### MainTabs (4 таба)
- **Portfolio** (PortfolioScreen) — общая стоимость, P&L, список AssetCard
- **Orders** (OrderListScreen) — ордера, цветовые статусы, infinite scroll, отмена
- **Accounts** (AccountListScreen) — счета, Sync/Disconnect, FAB "+ Connect"
- **Alerts** (NotificationScreen) — типы, иконки, mark-as-read, синяя полоса

### Модальные экраны
- **ConnectBrokerScreen** — chip-сетка брокеров, ввод номера счёта и токена
- **TradeOrderForm** — BUY/SELL, тикер, кол-во, цена, live total

## Компоненты

| Компонент | Описание |
|---|---|
| LoadingSpinner | ActivityIndicator по центру |
| ErrorMessage | Красная плашка с текстом ошибки |
| AssetCard | Карточка актива: тикер, P&L, qty, avg/current price |

## Содержание

- [screenshots.md](screenshots.md) — описание экранов
