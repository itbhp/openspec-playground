## Why

The Employee CRUD API currently persists data in an in-memory `ConcurrentHashMap`, so all data is lost on restart and cannot be shared across instances. This is Act 2 of the workshop: swap the persistence layer to MySQL via Spring Data JPA while proving, through the existing `EmployeeRepository` contract and its test suite, that the controller and service layers require zero changes.

## What Changes

- Add a JPA-based `JpaEmployeeRepository` adapter that implements the existing `EmployeeRepository` interface, backed by a Spring Data JPA repository and a MySQL database.
- Add JPA annotations to the `Employee` model (or an internal JPA entity mapped to/from the existing POJO — decided in design.md) so it can be persisted via Hibernate.
- Uncomment and activate the `spring-boot-starter-data-jpa` and `mysql-connector-j` dependencies in `build.gradle`.
- Uncomment and configure MySQL datasource properties in `application.properties`.
- Add a `compose.yaml` MySQL service (or extend the existing one) for local development.
- Replace `InMemoryEmployeeRepository` as the active `@Repository` bean with the new JPA adapter. **BREAKING** for local dev workflows that relied on the zero-setup in-memory store — MySQL (or Testcontainers) is now required to run the app and its integration tests.
- Add Testcontainers-based integration tests that run the existing `EmployeeRepositoryContractTest` contract suite against the JPA adapter with a real MySQL container.

## Capabilities

### New Capabilities
- `employee-persistence`: The storage-agnostic contract that any `EmployeeRepository` implementation must satisfy (CRUD semantics, id assignment, not-found handling). Not previously documented as a spec; extracted now so the MySQL adapter (and the future DynamoDB adapter) can be verified against the same requirements as the in-memory implementation.

### Modified Capabilities
- (none — no existing specs cover employee persistence yet; this is the first spec for it)

## Impact

- **Affected code**: new `com.systemservices.kata.repository.JpaEmployeeRepository` (adapter) and a Spring Data `JpaEmployeeEntityRepository`; `Employee` model or a new JPA entity class; Spring `@Repository`/`@Primary` bean wiring.
- **Files that must NOT be modified**: `EmployeeController.java`, `EmployeeService.java`, `EmployeeRepository.java` (the interface). The whole point of the custom repository interface is that persistence swaps are invisible above the repository layer.
- **Dependencies**: activates `spring-boot-starter-data-jpa` and `mysql-connector-j` (already present but commented out in `build.gradle`); adds no new dependencies beyond what's pre-staged.
- **Infrastructure**: requires a MySQL instance for `bootRun` (via `compose.yaml`) and Testcontainers MySQL for the test suite (already a test dependency).
- **Active persistence implementation being replaced**: `InMemoryEmployeeRepository`, a `ConcurrentHashMap`-backed, non-persistent, single-instance store.
