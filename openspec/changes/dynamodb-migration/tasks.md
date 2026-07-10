## 1. Dependencies and configuration

- [ ] 1.1 Uncomment `software.amazon.awssdk:dynamodb` and `software.amazon.awssdk:url-connection-client` in `build.gradle`; run `./gradlew compileJava` to confirm it compiles
- [ ] 1.2 Add `aws.dynamodb.endpoint` and `aws.dynamodb.region` properties to `application.properties`, matching `compose.yaml`'s `localstack` service (endpoint `http://localhost:4570`, region `us-east-1`)
- [ ] 1.3 Verify `compose.yaml`'s existing `localstack` service starts and is reachable via `docker compose up localstack` (no changes expected — already staged)

## 2. DynamoDB client wiring

- [ ] 2.1 Add `DynamoDbConfig` `@Configuration` class producing a `DynamoDbClient` bean from `aws.dynamodb.endpoint`/`aws.dynamodb.region` properties, using static test credentials for local/LocalStack use
- [ ] 2.2 Compile-check: `./gradlew compileJava` succeeds

## 3. DynamoDB repository adapter

- [ ] 3.1 Add `DynamoDbEmployeeRepository implements EmployeeRepository`: ensure the `employees` table exists on construction (create if missing, `PAY_PER_REQUEST` billing, partition key `id`)
- [ ] 3.2 Implement id generation via an atomic counter item (`UpdateItem` with `ADD` expression) keyed by a reserved partition key (`EMPLOYEE_ID_SEQ`) that cannot collide with a `Long` employee id
- [ ] 3.3 Implement `save`, `findById`, `findAll` (via `Scan`, filtering out the counter item), `deleteById`, `existsById` against the low-level `DynamoDbClient`, mapping `Employee` to/from `Map<String, AttributeValue>` manually (no annotations on `Employee`)
- [ ] 3.4 Annotate `DynamoDbEmployeeRepository` with `@Repository`; remove the `@Repository` stereotype from `JpaEmployeeRepository` (keep the class itself — do not delete)
- [ ] 3.5 Confirm `EmployeeRepository.java`, `Employee.java`, `EmployeeController.java`, and `EmployeeService.java` were not modified — diff check against `main`

## 4. Contract test

- [ ] 4.1 Add `DynamoDbEmployeeRepositoryTest extends EmployeeRepositoryContractTest`, annotated `@SpringBootTest` with a static Testcontainers `LocalStackContainer` (DynamoDB service enabled) and `@DynamicPropertySource` overriding `aws.dynamodb.endpoint`/`aws.dynamodb.region`
- [ ] 4.2 Implement `createRepository()` in `DynamoDbEmployeeRepositoryTest` by autowiring the `DynamoDbEmployeeRepository` bean
- [ ] 4.3 Run `DynamoDbEmployeeRepositoryTest`; all inherited contract scenarios (findAll empty/populated/snapshot isolation, id assignment incl. distinctness, update-in-place, findById, existsById, deleteById incl. no-op on missing id) must pass against LocalStack DynamoDB
- [ ] 4.4 Run `JpaEmployeeRepositoryTest` and `InMemoryEmployeeRepositoryTest` to confirm the dormant Act 1/Act 2 contract suites still pass unchanged

## 5. End-to-end verification

- [ ] 5.1 Add a second `TestRestTemplate` + `RANDOM_PORT` integration test (or extend the existing `EmployeeApiIntegrationTest` pattern) exercising the full HTTP stack (create → get → update → delete) against the Testcontainers-backed LocalStack DynamoDB repository
- [ ] 5.2 Manually run `docker compose up` (mysql + localstack) + `./gradlew bootRun`, exercise `/employees` CRUD via curl against the DynamoDB-backed app, restart the app process, and confirm previously created employees are still returned by `GET /employees` (verifies the updated "Durable storage" requirement)
- [ ] 5.3 Run the full suite — `./gradlew test` — and confirm all tests (unit, in-memory contract, JPA contract, DynamoDB contract, HTTP integration) pass with no skips or failures
