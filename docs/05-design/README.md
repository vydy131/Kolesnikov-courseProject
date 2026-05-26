# 05. Детальное проектирование

## Обзор

Этап детального проектирования включает диаграммы последовательностей для каждого варианта использования, показывающие взаимодействие компонентов от UI до БД.

## Паттерны проектирования

| Паттерн | Применение |
|---|---|
| Front Controller | DispatcherServlet -> контроллеры |
| Service Layer (Mediator) | Бизнес-логика в сервисах |
| Repository Pattern | Spring Data JPA, авто-генерация запросов |
| DTO Pattern | Request/response DTO на границе API |
| Factory Pattern | Создание User + Portfolio при регистрации |
| Strategy Pattern | BrokerClient адаптирует вызовы по api_base |
| Observer Pattern | Автоматическое уведомление при исполнении ордера |
| Singleton | Spring beans (default scope) |

## Содержание

- [sequence-diagrams.md](sequence-diagrams.md) — диаграммы последовательностей SF-01 — SF-06
