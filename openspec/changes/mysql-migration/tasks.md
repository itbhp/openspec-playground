## 1. Dependencies and configuration

- [ ] 1.1 Uncomment `spring-boot-starter-data-jpa` and `com.mysql:mysql-connector-j` in `build.gradle`; run `./gradlew build` to confirm it compiles (Testcontainers deps already present, no change needed)
- [ ] 1.2 Uncomment and fill in MySQL datasource properties in `application.properties` (URL, username, password, `spring.jpa.hibernate.ddl-auto=update`)
- [ ] 1.3 Add a MySQL service to `compose.yaml` for local `bootRun`; verify `docker compose up` starts a reachable MySQL instance

## 2. Entity mapping

- [ ] 2.1 Add `@Entity`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`, and column annotations to `Employee` (per design.md — no separate entity class)
- [ ] 2.2 Compile-check: `./gradlew compileJava` succeeds with the annotated `Employee`

## 3. JPA repository adapter

- [ ] 3.1 Add `SpringDataEmployeeJpaRepository extends JpaRepository<Employee, Long>` (package-private, not exposed outside `repository` package)
- [ ] 3.2 Add `JpaEmployeeRepository implements EmployeeRepository`, delegating each of the five interface methods to `SpringDataEmployeeJpaRepository`; ensure `deleteById` on a missing id does not throw (catch/guard per design.md Open Questions)
- [ ] 3.3 Annotate `JpaEmployeeRepository` with `@Repository`; remove the `@Repository` stereotype from `InMemoryEmployeeRepository` (keep the class itself — do not delete)
- [ ] 3.4 Confirm `EmployeeRepository.java` (the interface) was not modified — diff check against `main`
- [ ] 3.5 Confirm `EmployeeController.java` and `EmployeeService.java` were not modified — diff check against `main`

## 4. Contract test fix and JPA contract test

- [ ] 4.1 Update `EmployeeRepositoryContractTest`'s `@BeforeEach` to drain any pre-existing rows via `repository.findAll()` + `deleteById` after `createRepository()` (per design.md)
- [ ] 4.2 Run `InMemoryEmployeeRepositoryTest` to confirm the cleanup change doesn't break the existing in-memory contract suite
- [ ] 4.3 Add `JpaEmployeeRepositoryTest extends EmployeeRepositoryContractTest`, annotated `@SpringBootTest` with a static Testcontainers `MySQLContainer` and `@DynamicPropertySource` overriding `spring.datasource.*`
- [ ] 4.4 Implement `createRepository()` in `JpaEmployeeRepositoryTest` by autowiring the `JpaEmployeeRepository` bean
- [ ] 4.5 Run `JpaEmployeeRepositoryTest`; all inherited contract scenarios (findAll empty/populated/snapshot isolation, id assignment, update-in-place, findById, existsById, deleteById incl. no-op on missing id) must pass against real MySQL

## 5. End-to-end verification

- [ ] 5.1 Add/verify a `TestRestTemplate` + `RANDOM_PORT` integration test exercising the full HTTP stack (create → get → update → delete) against the Testcontainers-backed MySQL repository, per the project's integration-test convention
- [ ] 5.2 Manually run `docker compose up` + `./gradlew bootRun`, exercise `/employees` CRUD via curl, restart the app, and confirm previously created employees are still returned by `GET /employees` (verifies the "Durable storage" requirement)
- [ ] 5.3 Run the full suite — `./gradlew test` — and confirm all tests (unit, in-memory contract, JPA contract, HTTP integration) pass with no skips or failures
