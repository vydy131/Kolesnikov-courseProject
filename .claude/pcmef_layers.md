# PCMEF Architecture

## Client (Mobile)
- Presentation: UI components
- State Management: MobX stores
- API Client: HTTP communication
- Local Cache: offline mode

## Server
- Control: REST controllers
- Mediator: business logic services
- Entity: domain model
- Foundation: repositories + DB access

## Rule
Strict top-down dependency:
Presentation → Control → Mediator → Entity → Foundation
