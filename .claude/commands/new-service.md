Create a new Spring Boot service class for the Investment Aggregator Platform.

Service name: $ARGUMENTS

Follow these rules strictly:

1. Place the service in `backend/src/main/java/com/investagg/service/`
2. Annotate with `@Service` and `@RequiredArgsConstructor`
3. Mark class-level `@Transactional(readOnly = true)`; override individual write methods with `@Transactional`
4. Inject dependencies via `private final` fields (constructor injection via Lombok)
5. All method return types must be DTOs — never return raw JPA Entity objects
6. Throw named exceptions: `EntityNotFoundException`, `ConflictException`, `ForbiddenException` — not raw `RuntimeException`
7. This is the **Mediator layer** — all business logic belongs here
8. Never access a Repository from a Controller; never call another Service's internal helpers directly

After creating the service, also create:
- Request DTO(s) in `backend/src/main/java/com/investagg/dto/request/` (if new inputs needed)
- Response DTO(s) in `backend/src/main/java/com/investagg/dto/response/` (if new outputs needed)
- A stub `@RestController` in `backend/src/main/java/com/investagg/controller/` wired to this service (thin — just delegates)

Check `.claude/use_cases.md` for business requirements.
Check `.claude/methods_catalog.md` for existing method signatures to keep consistency.
Check `.claude/api_contracts.md` for expected request/response shapes.
