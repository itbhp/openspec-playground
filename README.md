# Workshop: Spec-Driven Development vs Vibe Coding
**Duration:** Half day (~3.5h) · **Stack:** Java · Spring Boot · Gradle · OpenAPI 3.1 · openapi-generator · Testcontainers

---

## Narrative Arc

> Act 1 — *"Just ask the AI"* → capable but inconsistent  
> Act 2 — *"Give the AI a contract"* → MySQL persistence, spec-owned  
> Act 3 — *"The spec remembers"* → DynamoDB refactor, AI-driven from the existing spec

The domain (Employee CRUD) never changes. The persistence does. That's the point.

---

## Prerequisites (attendees bring)
- JDK 21+, Gradle 8+, Docker (running)
- opencode installed and working
- Their usual editor/IDE

You provide:
- The skeleton project (zip / shared repo)
- This document

---

## Schedule

| Time | Segment |
|------|---------|
| 0:00 | Framing talk (20 min) |
| 0:20 | **Act 1** — Vibe coding (30 min) |
| 0:50 | Debrief Act 1 + mini-lecture: OpenAPI 3.1 (25 min) |
| 1:15 | **Act 2** — Spec v1: in-memory → MySQL (75 min) |
| 2:30 | Debrief Act 2 (15 min) |
| 2:45 | **Act 3** — Spec v2: MySQL → DynamoDB (30 min) |
| 3:15 | Final discussion (15 min) |

---

## Framing Talk (20 min)

No hands-on. Cover:

- What "vibe coding" means in practice: prompt → working code → ship
- What spec-driven development means: define the contract first, generate/implement against it
- Why the distinction matters for backend teams: contracts between services, teams, consumers
- The two claims you'll prove today:
    1. A spec makes AI output more predictable and correct
    2. A spec acts as persistent memory — it lets AI drive non-trivial refactors it would otherwise get wrong

Do **not** explain OpenAPI syntax yet. That comes after Act 1.

---

## Act 1 — Vibe Coding (30 min)

### Goal
Experience what AI-assisted development looks like with no contract.

### Setup
Attendees open the skeleton project. Walk them through it briefly (5 min):
- `Employee.java` — the POJO, fully written
- `EmployeeController.java` — all five operations stubbed, in-memory `Map` persistence
- `EmployeeService.java` — delegates to an in-memory `EmployeeRepository` interface
- Tests do not exist yet

Everything compiles and runs. `GET /employees` returns an empty list.

### The exercise
> "Use opencode to add whatever you think is missing or could be better. Improve the API. Make it production-ready. You have 25 minutes."

No further instructions. They prompt freely.

### What to observe (you circulate)
- Do they add validation? What field names do they use for error responses?
- Do different people produce different response shapes for the same error case?
- Does anyone add pagination? If so, what does the response envelope look like?
- Do any tests get written? If so, what do they test against?

### Debrief (part of the 25 min mini-lecture slot)
Ask two questions before moving on:
- "Show me your 404 response body. Now show your neighbour's."
- "If we had to write a client that works against both — what breaks?"

**Point to land:** Fast, capable, inconsistent. Every run produces a different API surface. A client team would be blocked. The AI needs a contract.

---

## Mini-Lecture: OpenAPI 3.1 Concepts (15 min)

Keep it tight. Only what they need for Act 2.

### Top-level structure
```
openapi · info · paths · components
```

### Paths and operations
```yaml
paths:
  /employees/{id}:
    get:
      operationId: getEmployee
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: Found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Employee'
        '404':
          $ref: '#/components/responses/NotFound'
```
- `operationId` → Java method name. Missing = ugly generated code.
- Path parameters go in `parameters`, not in the path string alone.

### Schemas
```yaml
components:
  schemas:
    Employee:
      type: object
      required: [firstName, lastName, email]
      properties:
        id:
          type: integer
          format: int64
        firstName:
          type: string
        email:
          type: string
          format: email
```
- `required` is a list on the object, not a property flag.
- Two models: `Employee` (read, has `id`) and `EmployeeRequest` (write, no `id`).

### Reusable responses
```yaml
components:
  responses:
    NotFound:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```
Define `NotFound`, `BadRequest` once, `$ref` everywhere.

That's all they need. Move on.

---

## Act 2 — Spec v1: In-Memory → MySQL (75 min)

### Goal
Write `employee-api-v1.yaml`, generate the server stubs, swap the persistence layer from in-memory to MySQL, and verify with Testcontainers.

### Phase 2a — Write the spec with opencode (20 min)

They create `src/main/resources/openapi/employee-api-v1.yaml`.

Starter prompt to give them:
> "Write an OpenAPI 3.1 spec for the Employee API in this project. Employees have id (int64), firstName, lastName, email (format: email), and department. Full CRUD: list all, create, get by id, update by id, delete by id. Separate EmployeeRequest schema (no id) for write operations. Reusable ErrorResponse schema with code (integer) and message (string). Reusable 404 and 400 responses in components. All operations must have an operationId."

**Key instruction you give verbally:** Every time opencode produces YAML, read it before accepting. You are the author. opencode is the typist.

They then iterate — add a `department` query param to `GET /employees`, adjust status codes, add field descriptions. Each change is a deliberate spec decision.

### Phase 2b — Generate stubs (10 min)

```bash
./gradlew openApiGenerate
```

The `build.gradle` is already wired. They inspect the generated interface and POJOs under `build/generated/`. Then make `EmployeeController` implement the generated interface — replacing the existing stub signatures.

### Phase 2c — MySQL persistence (30 min)

Replace the in-memory `EmployeeRepository` with a JPA implementation.

They need to:
- Add `@Entity` to `Employee`, add `@Id @GeneratedValue`
- Create `EmployeeJpaRepository extends JpaRepository<Employee, Long>`
- Implement `EmployeeService` methods against the JPA repository
- Add `application.properties` datasource config (point to localhost for now — Testcontainers will override in tests)

Dependencies are already in `build.gradle`:
```
spring-boot-starter-data-jpa
mysql-connector-j
```

### Phase 2d — Tests with Testcontainers (15 min)

They write `EmployeeControllerIT.java`:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class EmployeeControllerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired TestRestTemplate http;

    @Test
    void createAndRetrieve() { ... }

    @Test
    void getUnknownReturns404() { ... }
}
```

Tests must cover at least: create → get, get unknown → 404, delete → get → 404.

```bash
./gradlew test
```

---

## Debrief Act 2 (15 min)

Questions:
- "What did opencode get right in the spec on the first prompt? What did you have to correct?"
- "What happened when you changed a field name in the spec and re-ran the generator?"
- "What does the compiler error tell you that a test wouldn't?"

**Points to land:**
- The spec is now the source of truth. The compiler enforces the contract, not discipline.
- opencode wrote the YAML correctly *because* it had precise requirements to work from.
- The test verifies behaviour against a real database — the spec verified the shape.

---

## Act 3 — Spec v2: MySQL → DynamoDB (30 min)

### Goal
Use the existing spec as context to drive a full persistence refactor to DynamoDB on LocalStack — with opencode doing the heavy lifting.

### The setup (5 min, you narrate)

> "We have a working, spec-documented API. The business wants to migrate to DynamoDB. In a vibe-coding world, you'd prompt opencode with a vague description and hope it remembers what the API does. In an SDD world, the spec *is* the memory. We write a second spec that says: same API, new persistence contract."

They create `src/main/resources/openapi/employee-api-v2.yaml`.

This spec is **identical to v1** except:
- `info.version: 2.0.0`
- Add an `x-persistence` extension field at the info level: `x-persistence: dynamodb`
- Add a description note on each operation referencing the DynamoDB access pattern (e.g. `get by partition key id`)

The extensions aren't used by the generator — they're **documentation for opencode**.

### The opencode prompt (10 min)

Attendees give opencode this prompt (you display it on screen):
> "I have an Employee REST API currently using MySQL + JPA. The spec is in `employee-api-v2.yaml`. Refactor the persistence layer to use DynamoDB via the AWS SDK v2. Use LocalStack for local development. Replace `EmployeeJpaRepository` with a `DynamoDbEmployeeRepository`. Keep all controller and service code unchanged. Update the Testcontainers test to use a LocalStack container instead of MySQL."

They watch and guide. Intervene when opencode drifts from the spec.

### What opencode needs to produce (15 min to review + fix)
- Remove `spring-boot-starter-data-jpa` and `mysql-connector-j` from `build.gradle`
- Add `software.amazon.awssdk:dynamodb` and `org.testcontainers:localstack`
- `DynamoDbEmployeeRepository` implementing the same interface
- Updated `EmployeeControllerIT` using `LocalStackContainer`

```java
@Container
static LocalStackContainer localstack =
    new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(DYNAMODB);
```

```bash
./gradlew test
```

Same tests. Same spec. Different database. Green.

### The point (you state it explicitly)
> "opencode didn't need to re-understand the API. The spec told it what the contract was. It only had to solve the persistence problem — which is what we actually wanted."

---

## Final Discussion (15 min)

Three questions:

1. **"When would you still vibe-code?"**
   Let them draw the line. Prototypes, throwaway scripts, one-consumer internal tools are reasonable answers.

2. **"Who owns the spec in your team?"**
   This is a process question. The spec is a communication artefact. It lives in the repo, it's reviewed in PRs, it's versioned. Who writes the first draft? Who approves changes?

3. **"What changes about how you prompt opencode going forward?"**
   Expected landing: lead with the spec, not with a description. The spec is the most precise prompt you can give for any API-related task.

---

## What This Workshop Deliberately Does Not Cover
- Security schemes / OAuth flows
- Webhooks
- Polymorphism (`oneOf`, `anyOf`)
- Spec linting (Spectral)
- Consumer-driven contract testing (Pact)

These are follow-up workshops. Don't dilute the message today.