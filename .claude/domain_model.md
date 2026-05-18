Entities:
- User
- InvestmentAccount
- Portfolio
- Asset
- TradeOrder
- Transaction
- Broker
- Notification
- MarketData
- Report

Relationships:
User → Accounts (1:M)
User → Portfolio (1:1)
Portfolio → Assets (1:M)
Account → Orders (1:M)
Account → Transactions (1:M)
Broker → Accounts (1:M)
