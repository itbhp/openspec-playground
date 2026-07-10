## Context

`EmployeeRepository` is the only seam the controller and service depend on, so this migration is scoped entirely to a new adapter plus wiring — exactly as Act 2 (MySQL/JPA) was. `Employee` currently carries JPA annotations (`@Entity`, `@Id`, `@GeneratedValue(strategy = IDENTITY)`) added in Act 2. DynamoDB introduces a genuinely different constraint the JPA phase didn't have: no native auto-increment id, and no requirement (unlike JPA) that the domain class itself carry persistence annotations.

`build.gradle` has AWS SDK v2 `dynamodb` and `url-connection-client` dependencies pre-staged and commented out since Act 1. `compose.yaml` already has a `localstack` service (`SERVICES: dynamodb`) that has never been used until now. `org.testcontainers:localstack` is already an active test dependency (staged alongside `mysql` from the start).

## Goals / Non-Goals

**Goals:**
- Persist employees in DynamoDB via AWS SDK v2, with zero changes to `EmployeeController`, `EmployeeService`, `EmployeeRepository`, or `Employee`.
- Prove correctness by running the existing `EmployeeRepositoryContractTest` against the new adapter using Testcontainers LocalStack.
- Keep `JpaEmployeeRepository` (and `InMemoryEmployeeRepository`) in the codebase, dormant, not deleted — consistent with how Act 2 treated Act 1.

**Non-Goals:**
- No data migration from MySQL — there is no production data to carry over in this workshop.
- No multi-region / multi-table DynamoDB design — a single table, single region (matching `compose.yaml`'s `DEFAULT_REGION: us-east-1`) is sufficient.
- No use of the DynamoDB Enhanced Client / object-mapper annotations — see Decisions below.

## Decisions

**Use the low-level `DynamoDbClient`, not the Enhanced Client, and keep `Employee` free of AWS annotations.**
JPA required annotating `Employee` because Hibernate needs entity metadata on the class itself. DynamoDB's low-level SDK has no such requirement — the adapter alone can translate `Employee` to/from a `Map<String, AttributeValue>`. Alternative considered: `dynamodb-enhanced` with `@DynamoDbBean`/`@DynamoDbPartitionKey` on `Employee`, mirroring the JPA approach. Rejected — stacking a third persistence-annotation set (JPA + DynamoDB Enhanced) onto one POJO is exactly the kind of coupling the custom `EmployeeRepository` interface exists to avoid, and the low-level client's manual mapping is a handful of lines for a 5-field POJO. `Employee.java` is not touched by this change at all.

**Id generation via a DynamoDB atomic counter, not UUIDs.**
The contract (`EmployeeRepositoryContractTest`) requires `Long` ids, generated on save when absent, distinct across saves. DynamoDB has no auto-increment, so `DynamoDbEmployeeRepository` maintains a dedicated counter item (partition key `"EMPLOYEE_ID_SEQ"`) in the same table and increments it via `UpdateItem` with an `ADD` expression (`ReturnValues.UPDATED_NEW`), which DynamoDB guarantees is atomic under concurrent writers. Alternative considered: random UUIDs coerced into `Long` via hashing — rejected, since the contract's "successive new employees receive distinct ids" scenario is trivially satisfiable but hashing UUIDs into longs risks collisions and adds no value over an atomic counter for this workshop's scale.

**Single DynamoDB table, `employees`, partition key `id` (String). Counter lives in the same table as a special item, not a separate table.**
Keeps infrastructure to "one table" for a workshop-sized app. Alternative considered: a separate `employee_counters` table — rejected as unnecessary operational overhead for one counter. **Correction made during implementation**: a DynamoDB table's partition key has one fixed type for every item, and the reserved counter item's key (`"EMPLOYEE_ID_SEQ"`) is non-numeric — so `id` must be typed `S` (String) at table creation, not `N` (Number) as originally planned. Employee ids are still `Long` in `Employee`/`EmployeeRepository`; the adapter stores/reads them as their decimal string representation (`String.valueOf(id)` / `Long.valueOf(item.get("id").s())`).

**Table is created on repository startup if it doesn't exist (`CreateTable`, idempotent check first).**
Mirrors the zero-setup feel of `InMemoryEmployeeRepository` and avoids a manual provisioning step for the workshop. This is a workshop simplification, not a production pattern (see Risks).

**`DynamoDbClient` bean built from `@Value`-injected properties, not AWS's default credential/region chain.**
A new `DynamoDbConfig` `@Configuration` class reads `aws.dynamodb.endpoint` (optional — if blank, falls back to the client's default AWS endpoint resolution) and `aws.dynamodb.region` from `application.properties`, matching the pattern of explicit config already used for the MySQL datasource rather than relying on implicit environment-based AWS SDK configuration. Static test credentials (`test`/`test`) are used for LocalStack, matching `compose.yaml`'s LocalStack service (no real AWS credentials needed for local/dev/test).

**Only one `EmployeeRepository` bean active at a time (same pattern as Act 2 → Act 1).**
Remove the `@Repository` stereotype from `JpaEmployeeRepository` (keep the class — `JpaEmployeeRepositoryTest` still exercises it directly against Testcontainers MySQL). `DynamoDbEmployeeRepository` becomes the sole `@Repository` implementing `EmployeeRepository`.

**`EmployeeRepository` interface does not change.** All five methods map onto SDK v2 calls: `findAll` → `Scan`, `findById` → `GetItem`, `save` → id-assign-if-absent + `PutItem`, `deleteById` → `DeleteItem` (DynamoDB's `DeleteItem` is already a no-op on a missing key, matching the contract), `existsById` → `GetItem` (projected to the key only).

| Dependency | Action |
|---|---|
| `software.amazon.awssdk:dynamodb` | Add (uncomment in `build.gradle`) |
| `software.amazon.awssdk:url-connection-client` | Add (uncomment in `build.gradle`) — lightweight HTTP client for the SDK, avoids pulling in Netty |
| `org.testcontainers:localstack` | Already present (test scope) — no change |
| `org.springframework.boot:spring-boot-starter-data-jpa`, `com.mysql:mysql-connector-j` | No change — stay active so dormant `JpaEmployeeRepository` still compiles |

## Risks / Trade-offs

- **[Risk]** Auto-creating the table on startup is not a production pattern (no control over throughput mode, no IaC) → **Mitigation**: explicitly called out as a workshop simplification in this doc; a real project would provision the table via Terraform/CDK ahead of deploy.
- **[Risk]** The atomic-counter item shares the table with employee items — a `Scan` over all items (used by `findAll`) must filter it out → **Mitigation**: the counter item uses a reserved, non-numeric partition key (`"EMPLOYEE_ID_SEQ"`) that can never collide with a `Long` employee id, and `DynamoDbEmployeeRepository.findAll()` filters it out explicitly.
- **[Risk]** `Scan` for `findAll()` doesn't scale (full table read) → **Mitigation**: acceptable for a workshop-sized dataset; a production system would need a GSI or a different access pattern, out of scope here.
- **[Trade-off]** Testcontainers LocalStack makes the integration suite slower and requires Docker, same trade-off already accepted for the MySQL Testcontainers suite in Act 2.

## Migration Plan

No data migration is required (workshop has no production MySQL data to carry over). Cutover is a code-and-config change:
1. Uncomment DynamoDB SDK dependencies; add `DynamoDbConfig`.
2. Add DynamoDB endpoint/region properties to `application.properties`, matching `compose.yaml`'s `localstack` service.
3. Add `DynamoDbEmployeeRepository` (table-ensure-exists on construction, atomic counter, CRUD via low-level client); remove `@Repository` from `JpaEmployeeRepository`.
4. Add `DynamoDbEmployeeRepositoryTest extends EmployeeRepositoryContractTest` using Testcontainers LocalStack.
5. Run full suite (unit + both Testcontainers integration suites) before merging.

**Rollback**: re-add `@Repository` to `JpaEmployeeRepository`, remove it from `DynamoDbEmployeeRepository` — Spring's bean wiring reverts instantly since the interface never changed. No data to roll back.

## Open Questions

- Should the DynamoDB table use on-demand (`PAY_PER_REQUEST`) or provisioned billing mode for local/dev? (Design assumes `PAY_PER_REQUEST` for zero-config startup, matching LocalStack's defaults.)
- Should `findAll()`'s `Scan`-based filtering of the counter item be done client-side (simplest) or via a `FilterExpression` server-side? (Design assumes client-side filtering for a single reserved key — negligible cost at workshop scale.)
