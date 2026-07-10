## Why

This is Act 3 of the workshop: prove that the `EmployeeRepository` abstraction built in Act 1 and exercised in Act 2 lets us swap the backing store again — this time to DynamoDB via AWS SDK v2 — with zero changes above the repository layer, using the spec (not tribal memory) as the source of truth for what "correct" persistence behavior means.

## What Changes

- Add a `DynamoDbEmployeeRepository` adapter implementing the existing `EmployeeRepository` interface, backed by the AWS SDK v2 low-level `DynamoDbClient` (no annotation-based mapper — see design.md for why `Employee` stays free of AWS-specific annotations, unlike the JPA phase).
- Add an atomic-counter-based id generator in DynamoDB to replace JPA's `IDENTITY` generation strategy, since DynamoDB has no native auto-increment.
- Add a `DynamoDbConfig` `@Configuration` class producing the `DynamoDbClient` bean, pointed at LocalStack for local dev/tests and real AWS in other environments.
- Uncomment and activate the `software.amazon.awssdk:dynamodb` and `software.amazon.awssdk:url-connection-client` dependencies in `build.gradle` (already staged, commented out since Act 1).
- Add AWS/DynamoDB endpoint and region properties to `application.properties`, wired to the existing `localstack` service in `compose.yaml` (already present, unused until now).
- Replace `JpaEmployeeRepository` as the active `@Repository` bean with `DynamoDbEmployeeRepository`. **BREAKING** for local dev workflows that relied on MySQL — LocalStack (or AWS) is now required to run the app and its integration tests; MySQL is no longer needed for `bootRun` or `test`.
- Add a Testcontainers LocalStack-based integration test that runs the existing `EmployeeRepositoryContractTest` contract suite against the DynamoDB adapter.
- Keep `JpaEmployeeRepository` and `InMemoryEmployeeRepository` in the codebase (dormant, not deleted, not de-annotated) — same pattern used when Act 2 superseded Act 1.

## Capabilities

### New Capabilities
(none — the storage-agnostic contract from Act 2 already covers this)

### Modified Capabilities
- `employee-persistence`: the "Durable storage" requirement currently says data persists "when the active `EmployeeRepository` implementation is backed by MySQL." This is now implementation-specific and stale once DynamoDB becomes the active store — it needs to generalize to "any durable (non-in-memory) backing store."

## Impact

- **Affected code**: new `com.systemservices.kata.repository.DynamoDbEmployeeRepository` (adapter) and `com.systemservices.kata.config.DynamoDbConfig`; an internal id-counter helper (part of the adapter, not a new public type).
- **Files that must NOT be modified**: `EmployeeController.java`, `EmployeeService.java`, `EmployeeRepository.java` (the interface), `Employee.java` (no new persistence annotations needed — see design.md).
- **Dependencies**: activates `software.amazon.awssdk:dynamodb` and `software.amazon.awssdk:url-connection-client` (already present but commented out in `build.gradle`); `org.testcontainers:localstack` is already an active test dependency.
- **Infrastructure**: requires LocalStack for `bootRun` (via the existing `compose.yaml` service) and Testcontainers LocalStack for the test suite (already staged).
- **Active persistence implementation being replaced**: `JpaEmployeeRepository`, a Spring Data JPA adapter backed by MySQL (added in Act 2, becomes dormant).
