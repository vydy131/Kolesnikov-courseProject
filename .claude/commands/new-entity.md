Create a new JPA entity for the Investment Aggregator Platform backend.

Entity name: $ARGUMENTS

Follow these rules strictly:

1. Place the entity in `backend/src/main/java/com/investagg/entity/`
2. Annotate with `@Entity`, `@Table(name = "snake_case_plural")`
3. Use Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`
4. Primary key: `UUID id` with `@GeneratedValue(strategy = GenerationType.UUID)`
5. Include `created_at` as `OffsetDateTime` with `@Column(updatable = false)`
6. Add `deleted_at OffsetDateTime` if the entity is a financial/critical record (accounts, orders, transactions, users)
7. All relationships must use `FetchType.LAZY`
8. Use `CascadeType.ALL` only for aggregate roots
9. Never put business logic inside the entity class

After creating the entity, also:
- Create the corresponding Spring Data repository interface in `backend/src/main/java/com/investagg/repository/`
- Create a Flyway migration SQL file `V{next_number}__create_{table_name}_table.sql` in `backend/src/main/resources/db/migration/`

Check `.claude/database_model.md` for column naming and constraint conventions.
Check `.claude/domain_model.md` to ensure relationships match the domain model.
