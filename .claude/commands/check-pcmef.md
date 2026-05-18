Review the provided code or file path for PCMEF architecture compliance.

Target: $ARGUMENTS

Perform the following checks and report each finding clearly:

## Checklist

**Control layer (Controllers):**
- [ ] Controller does NOT call a Repository directly
- [ ] Controller delegates all logic to a Service
- [ ] Controller uses `@Valid` for request body validation
- [ ] Controller returns DTO, not raw Entity
- [ ] No business calculations in Controller methods

**Mediator layer (Services):**
- [ ] Business logic is in the Service, not in Controller or Repository
- [ ] Service does NOT import or use `@Repository` beans directly from another service in a way that bypasses its own service
- [ ] All external API calls go through a dedicated `*Client` class
- [ ] Write methods are annotated with `@Transactional`

**Entity layer:**
- [ ] Entity class does NOT import any Service or Repository
- [ ] Entity uses `FetchType.LAZY` for all relationships
- [ ] Entity contains NO business logic methods

**Foundation layer (Repositories):**
- [ ] Repository contains only query methods — no business logic
- [ ] Custom `@Query` annotations are used only when Spring Data method naming is insufficient
- [ ] Soft-deleted entities are filtered with `AND deleted_at IS NULL`

**DTO layer:**
- [ ] All API responses use DTOs, not JPA Entities
- [ ] Request DTOs have `@Valid` annotations on fields where needed
- [ ] No sensitive data (passwords, tokens) appears in response DTOs

**Frontend (if applicable):**
- [ ] Screen component does NOT call Axios/fetch directly
- [ ] Screen component does NOT contain business logic
- [ ] All data access goes through MobX store → API Client

## Output Format

List each violation found as:
```
[VIOLATION] Layer: <layer> | File: <file> | Line: <line> | Issue: <description> | Fix: <suggestion>
```

If no violations, confirm:
```
[PASS] Code is PCMEF-compliant.
```

Reference `.claude/architecture.md` and `.claude/coding_guidelines.md` for the full rules.
