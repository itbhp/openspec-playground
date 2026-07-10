## Context

`Employee` is currently a persistence-agnostic POJO stored in a `ConcurrentHashMap` behind `InMemoryEmployeeRepository`. `EmployeeRepository` is a hand-written interface (not `Spring Data`), so the controller and service already depend only on an abstraction — the adapter is the only thing that needs to change. `AGENTS.md` notes `Employee` was deliberately kept as a mutable POJO (no records, no Lombok) specifically so it could later be annotated for JPA without a rewrite, so this design assumes direct annotation rather than introducing a parallel entity class.

`build.gradle` and `application.properties` already have MySQL/JPA config staged but commented out, and `spring-boot-starter-test` + Testcontainers MySQL are already test dependencies. The repository contract is captured in `EmployeeRepositoryContractTest`, an abstract JUnit test extended by `InMemoryEmployeeRepositoryTest` — the new adapter must pass the same contract suite.

## Goals / Non-Goals

**Goals:**
- Persist employees in MySQL via Spring Data JPA, with zero changes to `EmployeeController`, `EmployeeService`, or the `EmployeeRepository` interface.
- Prove correctness by running the existing `EmployeeRepositoryContractTest` against the new adapter using a real MySQL instance (Testcontainers).
- Keep `InMemoryEmployeeRepository` in the codebase (dormant, not deleted) per the project's "comment out, don't remove" convention for inactive phases.

**Non-Goals:**
- No schema migration tooling (Flyway/Liquibase) — out of scope for this phase; Hibernate DDL generation is acceptable for a workshop.
- No data migration from the in-memory store — there is no persistent data to migrate.
- No DynamoDB work (Act 3) — tracked separately.

## Decisions

**Annotate `Employee` directly with JPA, no separate entity class.**
`Employee` becomes `@Entity` with `@Id @GeneratedValue(strategy = IDENTITY)` on `id` and column mappings on the remaining fields. Alternative considered: keep `Employee` persistence-ignorant and map to/from a dedicated `EmployeeEntity`. Rejected — `AGENTS.md` already signals `Employee` was shaped for this, and the domain model has no fields JPA can't represent directly, so a mapper would be pure overhead.

**Spring Data repository is an internal implementation detail, wrapped by an adapter.**
Add `SpringDataEmployeeJpaRepository extends JpaRepository<Employee, Long>` (package-private to `repository`) and `JpaEmployeeRepository implements EmployeeRepository`, which delegates to it. Only `JpaEmployeeRepository` is exposed as a bean of type `EmployeeRepository`. This matches the existing convention ("no Spring Data repository interfaces exposed beyond the persistence layer") and keeps `save`'s current semantics (new id when absent, update-in-place when present) — `JpaRepository.save` already has this exact behavior, so no custom logic is needed there.

**Only one `EmployeeRepository` bean is active at a time.**
Remove the `@Repository` stereotype annotation from `InMemoryEmployeeRepository` (keep the class, since `InMemoryEmployeeRepositoryTest` instantiates it directly with `new` and needs no Spring context). `JpaEmployeeRepository` becomes the sole `@Repository` implementing `EmployeeRepository`, so Spring wires it into `EmployeeService` with no ambiguity — no `@Primary`/`@Qualifier` needed.

**`EmployeeRepository` interface does not change.**
All five methods (`findAll`, `findById`, `save`, `deleteById`, `existsById`) map directly onto `JpaRepository` equivalents. No new methods are needed for this migration.

**Contract test gets one small, backward-compatible fix: state cleanup between tests.**
`EmployeeRepositoryContractTest`'s `@BeforeEach` currently just calls `createRepository()`, which is fine for `InMemoryEmployeeRepository` (a fresh instance every time) but not for `JpaEmployeeRepository`, which is a Spring-managed singleton bean backed by a shared table. Fix: after `createRepository()`, drain any pre-existing rows via the interface itself (`repository.findAll().forEach(e -> repository.deleteById(e.getId()))`). This uses only contract methods, so it works identically for both implementations and requires no new test-only hooks.

**JPA integration test uses `@SpringBootTest` + Testcontainers, not `@DataJpaTest`.**
`JpaEmployeeRepositoryTest extends EmployeeRepositoryContractTest`, annotated `@SpringBootTest` with a static `@Container` MySQLContainer and `@DynamicPropertySource` overriding `spring.datasource.*`. `createRepository()` autowires and returns the singleton `JpaEmployeeRepository` bean. `@DataJpaTest` was considered and rejected — it swaps in an embedded/test datasource by default and doesn't naturally wire a hand-rolled `EmployeeRepository` adapter bean; a full Spring context keeps the test closest to production wiring.

| Dependency | Action |
|---|---|
| `org.springframework.boot:spring-boot-starter-data-jpa` | Add (uncomment in `build.gradle`) |
| `com.mysql:mysql-connector-j` | Add (uncomment in `build.gradle`, `runtimeOnly`) |
| `org.testcontainers:mysql` | Already present (test scope) — no change |
| DynamoDB / LocalStack deps | No change — remain commented out for Act 3 |

## Risks / Trade-offs

- **[Risk]** Hibernate auto-DDL (`spring.jpa.hibernate.ddl-auto=update`) can silently drift from a hand-written schema in real projects → **Mitigation**: acceptable here since there is no existing schema/data to protect; call this out explicitly as a workshop simplification, not a production pattern.
- **[Risk]** Running the app locally now requires MySQL instead of "just run it" → **Mitigation**: `compose.yaml` gets a MySQL service so `docker compose up` remains a one-command setup; documented in tasks.
- **[Risk]** Contract-test cleanup logic assumes `deleteById` on an unknown id is safe (already true for `InMemoryEmployeeRepository`; must hold for JPA too) → **Mitigation**: `JpaEmployeeRepository.deleteById` should no-op on a missing id (matches `JpaRepository.deleteById`'s existing behavior of throwing `EmptyResultDataAccessException` — this must be handled, see Open Questions).
- **[Trade-off]** Testcontainers MySQL makes the integration test suite slower and requires Docker to run tests locally/CI → accepted, since it's the only way to verify real MySQL behavior (this is the explicit point of Act 2).

## Migration Plan

No data migration is required (previous store was non-persistent). Cutover is a code-and-config change:
1. Uncomment JPA/MySQL dependencies and datasource properties.
2. Add JPA annotations to `Employee`.
3. Add `SpringDataEmployeeJpaRepository` and `JpaEmployeeRepository`; remove `@Repository` from `InMemoryEmployeeRepository`.
4. Add MySQL service to `compose.yaml`.
5. Fix contract test cleanup; add `JpaEmployeeRepositoryTest`.
6. Run full suite (unit + Testcontainers integration) before merging.

**Rollback**: re-add `@Repository` to `InMemoryEmployeeRepository` and remove/comment the `@Repository` stereotype on `JpaEmployeeRepository` — Spring's bean wiring reverts instantly since the interface never changed. No data to roll back.

## Open Questions

- Should `JpaEmployeeRepository.deleteById` catch `EmptyResultDataAccessException` to match the in-memory adapter's silent no-op on missing ids, or should the contract itself be tightened to allow implementations to throw? (Design assumes the former, for behavioral parity with `InMemoryEmployeeRepository`.)
- `ddl-auto` value for the `bootRun` profile vs. the test profile (`update` vs `create-drop`) — assumed `update` for dev, `create-drop` for Testcontainers-backed tests, to be confirmed during implementation.
