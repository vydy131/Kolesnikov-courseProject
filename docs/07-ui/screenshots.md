# Описание экранов

## 1. LoginScreen
- Центрированная форма на светлом фоне (#F5F7FA)
- Заголовок "InvestAgg", подзаголовок "Sign in to your account"
- Поля: Email (email-address keyboard), Password (secureTextEntry)
- Кнопка "Sign In" (#007AFF)
- Ссылка "Don't have an account? Register"

## 2. RegisterScreen
- Аналогичный дизайн, заголовок "Create Account"
- Валидация: пароль минимум 8 символов
- Кнопка "Register", ссылка "Already have an account? Sign In"

## 3. PortfolioScreen
- Тёмный header (#1A1A2E): Total Value, P&L (зелёный/красный), процент
- FlatList с AssetCard: тикер, стоимость, P&L, qty, avg/current price
- Pull-to-refresh (#007AFF)
- Empty state: "No assets in portfolio yet."

## 4. OrderListScreen
- Карточки ордеров с direction badge (BUY зелёный, SELL красный)
- Статус: PENDING (оранжевый), FILLED (зелёный), CANCELLED (серый), REJECTED (красный)
- Qty, Price, Total в строке деталей
- Кнопка "Cancel Order" для PENDING-ордеров
- Infinite scroll (onEndReached)

## 5. AccountListScreen
- Карточки: brokerName, status badge, accountNumber, syncedAt
- Кнопки "Sync" (синяя рамка) и "Disconnect" (красная рамка, с Alert-подтверждением)
- FAB "+ Connect Broker" (#007AFF) внизу экрана

## 6. ConnectBrokerScreen
- Сетка broker chip-кнопок (Tinkoff, Sberbank и др.)
- Поля: Account Number, Broker API Token (secureTextEntry)
- Кнопка "Connect Account" с loading state

## 7. TradeOrderForm
- Информация о счёте (brokerName + accountNumber)
- Поле Ticker (autoCapitalize: characters)
- Segmented control BUY/SELL (зелёный/красный)
- Поля Quantity и Limit Price (decimal-pad)
- Live total: "Total: {qty * price}"
- Кнопка "Place BUY/SELL Order" с loading state

## 8. NotificationScreen
- Карточки: иконка по типу, title, body, timestamp
- Непрочитанные: синяя полоса слева (borderLeftColor #007AFF) + синяя точка
- Tap -> markRead
- Pull-to-refresh
- Tab badge показывает unreadCount
