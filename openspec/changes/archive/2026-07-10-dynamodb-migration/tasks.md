## 1. Dependencies and configuration

- [x] 1.1 Uncomment `software.amazon.awssdk:dynamodb` and `software.amazon.awssdk:url-connection-client` in `build.gradle`; run `./gradlew compileJava` to confirm it compiles
- [x] 1.2 Add `aws.dynamodb.endpoint` and `aws.dynamodb.region` properties to `application.properties`, matching `compose.yaml`'s `localstack` service (endpoint `http://localhost:4570`, region `us-east-1`)
- [x] 1.3 Verify `compose.yaml`'s existing `localstack` service starts and is reachable via `docker compose up localstack` (pinned image to `localstack/localstack:3.8.1` — newer/`latest` tags gate startup behind a Pro license)

## 2. DynamoDB client wiring

- [x] 2.1 Add `DynamoDbConfig` `@Configuration` class producing a `DynamoDbClient` bean from `aws.dynamodb.endpoint`/`aws.dynamodb.region` properties, using static test credentials for local/LocalStack use
- [x] 2.2 Compile-check: `./gradlew compileJava` succeeds

## 3. DynamoDB repository adapter

- [x] 3.1 Add `DynamoDbEmployeeRepository implements EmployeeRepository`: ensure the `employees` table exists on construction (create if missing, `PAY_PER_REQUEST` billing, partition key `id`, typed `S` — see design.md correction)
- [x] 3.2 Implement id generation via an atomic counter item (`UpdateItem` with `ADD` expression) keyed by a reserved partition key (`EMPLOYEE_ID_SEQ`) that cannot collide with a `Long` employee id
- [x] 3.3 Implement `save`, `findById`, `findAll` (via `Scan`, filtering out the counter item), `deleteById`, `existsById` against the low-level `DynamoDbClient`, mapping `Employee` to/from `Map<String, AttributeValue>` manually (no annotations on `Employee`)
- [x] 3.4 Annotate `DynamoDbEmployeeRepository` with `@Repository`; remove the `@Repository` stereotype from `JpaEmployeeRepository` (keep the class itself — do not delete)
- [x] 3.5 Confirm `EmployeeRepository.java`, `Employee.java`, `EmployeeController.java`, and `EmployeeService.java` were not modified — diff check against `main`

## 4. Contract test

- [x] 4.1 Add `DynamoDbEmployeeRepositoryTest extends EmployeeRepositoryContractTest`, annotated `@SpringBootTest` with a static Testcontainers `LocalStackContainer` (DynamoDB service enabled) and `@DynamicPropertySource` overriding `aws.dynamodb.endpoint`/`aws.dynamodb.region`
- [x] 4.2 Implement `createRepository()` in `DynamoDbEmployeeRepositoryTest` by autowiring the `DynamoDbEmployeeRepository` bean
- [x] 4.3 Run `DynamoDbEmployeeRepositoryTest`; all inherited contract scenarios (findAll empty/populated/snapshot isolation, id assignment incl. distinctness, update-in-place, findById, existsById, deleteById incl. no-op on missing id) pass against LocalStack DynamoDB
- [x] 4.4 Run `JpaEmployeeRepositoryTest` and `InMemoryEmployeeRepositoryTest` to confirm the dormant Act 1/Act 2 contract suites still pass — required re-enabling JPA autoconfiguration explicitly in `JpaEmployeeRepositoryTest` and mocking `DynamoDbClient` there (see Notes)

## 5. End-to-end verification

- [x] 5.1 Extend the existing `EmployeeApiIntegrationTest` (`TestRestTemplate` + `RANDOM_PORT`) to point at Testcontainers LocalStack instead of MySQL, since it exercises whichever `EmployeeRepository` bean is actually active — full HTTP CRUD lifecycle passes against DynamoDB
- [x] 5.2 Manually ran `docker compose up localstack` + `./gradlew bootRun`, exercised `/employees` CRUD via curl, killed and restarted the app process, confirmed the previously created employee was still returned by `GET /employees` (verifies the updated "Durable storage" requirement)
- [x] 5.3 Ran the full suite — `./gradlew clean test` — 55/55 tests pass, no skips or failures (unit, in-memory contract, JPA contract, DynamoDB contract, HTTP integration)

## Notes (discovered during implementation)

- **Partition key type correction**: `id` had to be typed `S` (String), not `N` (Number) as design.md originally specified — DynamoDB requires one fixed type per partition key across all items in a table, and the reserved counter item's key is non-numeric. `design.md` updated accordingly.
- **LocalStack image pin**: `compose.yaml` and both LocalStack-based tests pin `localstack/localstack:3.8.1`. The untagged/`latest`/`2026.x` images now refuse to start without a Pro license/auth token, even for community-tier services like DynamoDB.
- **App-wide JPA autoconfiguration gap (real bug, not just tests)**: with `JpaEmployeeRepository` dormant but `spring-boot-starter-data-jpa` still on the classpath, Spring Data JPA still scans `SpringDataEmployeeJpaRepository` and eagerly builds an `EntityManagerFactory`, which requires a reachable MySQL — so `./gradlew bootRun` failed to start without MySQL running, directly contradicting the proposal's stated goal. Fixed by disabling JPA/DataSource autoconfiguration by default in `application.properties` (`spring.autoconfigure.exclude=...HibernateJpaAutoConfiguration,...DataSourceAutoConfiguration`); `JpaEmployeeRepositoryTest` re-enables it explicitly for its own context via `@SpringBootTest(properties = "spring.autoconfigure.exclude=")`.
