JPA Strategy:

- FetchType.LAZY by default
- Cascade only for aggregates
- DTO separation mandatory

Patterns:
- @OneToMany (User → Accounts)
- @ManyToOne (Account → Broker)
- @OneToOne (User → Portfolio)

Inheritance:
- minimal usage (only notifications via SINGLE_TABLE)
