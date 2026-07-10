# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Half-day format (3.5h), three acts:
- **Act 1** (vibe coding): Write tests that would catch a broken persistence layer.
- **Act 2** (spec v1): Write MySQL via JPA + Testcontainers
- **Act 3** (spec v2): Refactor MySQL → DynamoDB using the spec as "persistent memory"

Domain: Employee CRUD REST API. Persistence changes, domain stays the same.

Stack: Java 21, Spring Boot 3.4.5, Gradle 9.3 wrapper, Testcontainers.

## Commands

```bash
./gradlew bootRun          # Run app (port 8080)
./gradlew build            # Compile + test + package
./gradlew test             # Run tests
```

No CI, no Makefile, no task runner. All commands are Gradle-only.

## Architecture

- Model: `Employee` POJO (mutable, not a record — intentional for JPA compat), now `@Entity`-annotated directly (Act 2)
- Repository: Custom `EmployeeRepository` interface, not Spring Data. Decoupled from persistence tech for clean phase transitions.
- Active impl: `JpaEmployeeRepository` (wraps a package-private `SpringDataEmployeeJpaRepository`), backed by MySQL
- Dormant impl: `InMemoryEmployeeRepository` (`ConcurrentHashMap` + `AtomicLong`) — no longer wired as a bean, kept for its contract test
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeeController` at `/employees` with full CRUD

Package: `com.systemservices.kata`

## Testing

43 tests across unit, repository-contract, and HTTP-integration levels. Test dependencies in `build.gradle`:
- `spring-boot-starter-test` (JUnit 5, MockMvc, Mockito) — JUnit Vintage engine excluded
- Testcontainers: `junit-jupiter`, `mysql`, `localstack` (BOM 1.21.4)

Layout:
- `EmployeeServiceTest` — Mockito-mocked repository unit tests
- `EmployeeControllerTest` — `@WebMvcTest` + `MockitoBean`-mocked service
- `EmployeeRepositoryContractTest` — abstract contract suite, extended by `InMemoryEmployeeRepositoryTest` and `JpaEmployeeRepositoryTest` (Testcontainers MySQL)
- `EmployeeApiIntegrationTest` — `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`, full HTTP stack over Testcontainers MySQL

Running the JPA/HTTP integration tests requires Docker.

## Conventions

- No Lombok, no records — plain JavaBeans with getters/setters
- Custom repository interface (not Spring Data) to keep persistence tech decoupled
- JPA/MySQL dependencies are active (Act 2 complete); DynamoDB dependencies remain commented out in `build.gradle`, ready for Act 3
- Config for LocalStack/DynamoDB is commented out in `application.properties`; MySQL datasource config is active
- `build.gradle` uses a `java { toolchain {...} }` block to pin the JDK version (Gradle 9.3 removed the top-level `sourceCompatibility` property)
- The workshop `README.md` (323 lines) is the authoritative guide — it contains the full exercise flow, prompts, and expected outcomes. Consult it for context on what each phase should produce.
