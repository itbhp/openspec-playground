# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Half-day format (3.5h), three acts:
- **Act 1** (vibe coding): Improve the API with AI, no spec — experience inconsistency
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

- Model: `Employee` POJO (mutable, not a record — intentional for JPA compat in later phases)
- Repository: Custom `EmployeeRepository` interface, not Spring Data. Decoupled from persistence tech for clean phase transitions.
- Impl: `InMemoryEmployeeRepository` (`ConcurrentHashMap` + `AtomicLong`)
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeeController` at `/employees` with full CRUD

Package: `com.systemservices.kata`

## Testing

Zero tests currently. Test dependencies pre-staged in `build.gradle`:
- `spring-boot-starter-test` (JUnit 5, MockMvc, Mockito)
- Testcontainers: `junit-jupiter`, `mysql`, `localstack` (BOM 1.19.8)

Test dirs exist but are empty: `src/test/java/com/systemservices/kata/`, `src/test/resources/`

## Conventions

- No Lombok, no records — plain JavaBeans with getters/setters
- Custom repository interface (not Spring Data) to keep persistence tech decoupled
- Dependencies for JPA and DynamoDB are commented out in `build.gradle`, ready for phase transitions
- Config for MySQL/LocalStack is commented out in `application.properties`
- `build.gradle` uses `java {}` block for sourceCompatibility (Gradle 9.3 removed top-level `sourceCompatibility` property)
- The workshop `README.md` (323 lines) is the authoritative guide — it contains the full exercise flow, prompts, and expected outcomes. Consult it for context on what each phase should produce.
