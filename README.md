# OpenSpec Workshop: Spec-Driven Persistence Migrations
**Duration:** Half day (~3.5h) · **Stack:** Java · Spring Boot · Gradle · Testcontainers

---

## What is OpenSpec?

[OpenSpec](https://openspec.dev) is a CLI tool and set of opencode skills that bring structure to AI-assisted development. Instead of ad-hoc prompts, you work through a defined workflow: **explore** the problem, **propose** a change (producing a proposal, design, and task list), **apply** the tasks, and **archive** the completed change. The specs and decisions are captured in files under `openspec/`, giving AI persistent context across sessions.

The four commands you'll use today:

| Command | Purpose |
|---------|---------|
| `/opsx-explore` | Think through a problem before committing to a direction |
| `/opsx-propose` | Create a change: generates proposal, design, and tasks |
| `/opsx-apply` | Implement the tasks from a change |
| `/opsx-archive` | Finalize and archive a completed change |

Install: [https://openspec.dev](https://openspec.dev)

---

## Narrative Arc

> Act 1 — *"Just ask the AI"* → capable but inconsistent
> Act 2 — *"Give the AI a structured change"* → MySQL migration via OpenSpec
> Act 3 — *"The specs remember"* → DynamoDB migration, building on archived context

The domain (Employee CRUD) never changes. The persistence does. That's the point.

---

## Prerequisites

- JDK 21+, Gradle 8+, Docker (running)
- opencode installed and working
- `openspec` CLI installed ([install guide](https://openspec.dev))
- Their usual editor/IDE

---

## Schedule

| Time | Segment |
|------|---------|
| 0:00 | Framing talk (20 min) |
| 0:20 | **Act 1** — Vibe coding (30 min) |
| 0:50 | Debrief Act 1 (15 min) |
| 1:05 | **Act 2** — MySQL migration with OpenSpec (80 min) |
| 2:25 | Debrief Act 2 (15 min) |
| 2:40 | **Act 3** — DynamoDB migration with OpenSpec (35 min) |
| 3:15 | Final discussion (15 min) |

---

## Framing Talk (20 min)

No hands-on. Cover:

- What "vibe coding" means in practice: prompt → working code → ship
- What spec-driven development means: define the change formally, let AI implement against it
- Why the distinction matters for backend teams: when you change infrastructure, you need precision — not hoping the AI remembers what the code does
- The two claims you'll prove today:
    1. A structured change makes AI output more predictable and correct — even for something as routine as test generation
    2. Captured specs act as persistent memory — AI can drive non-trivial refactors across sessions without losing context
- Quick demo of the OpenSpec workflow: `/opsx-propose` → review artifacts → `/opsx-apply`

---

## Act 1 — Vibe Coding (30 min)

### Goal

Experience what AI-assisted test generation looks like with no structured change.

### Setup

Attendees open the skeleton project. Walk them through it briefly (5 min):
- `Employee.java` — the POJO, fully written
- `EmployeeController.java` — all five operations stubbed, in-memory `Map` persistence
- `EmployeeService.java` — delegates to an in-memory `EmployeeRepository` interface
- No tests exist yet

Everything compiles and runs. `GET /employees` returns an empty list.

### The exercise

> "Use opencode to write unit tests for this project. You have 25 minutes."

No further instructions. They prompt freely — some will ask for controller tests, some for service tests, some for both. Let them discover what they get.

### Debrief (15 min)

Ask two questions:

- "What testing approach did you end up with? MockMvc? TestRestTemplate? Plain unit tests with mocks?"
- "Compare your test structure with your neighbour's. Could you swap implementations and keep the tests passing?"

**Point to land:** Fast, capable, inconsistent. Every run produces a different testing strategy and different coverage. When the persistence layer changes, these tests will break in unpredictable ways. The AI needs a structured way to work — one that separates "what to test" from "how to implement." That's what OpenSpec provides.

---

## Act 2 — MySQL Migration with OpenSpec (80 min)

### Goal

Use OpenSpec to structure a persistence migration from in-memory to MySQL, then implement it.

### The OpenSpec Workflow

Each change follows this cycle. Follow these steps — don't skip ahead.

1. **Create a branch** from main
   git checkout -b <your-branch-name>

2. **Propose** — describe what you want to change
   /opsx-propose <change-name>
   → Review the generated artifacts (proposal, design, tasks)
   → Edit if needed — you are the author, AI is the typist
   → Implementation will be as good as the context you provide in the spec
   → Commit the artifacts

3. **Apply** — let AI implement the tasks
   /opsx-apply <change-name>
   → Review each task's output before moving on
   → Run ./gradlew build to verify
   → Commit when all tasks are done

4. **Archive** — finalize the change
   /opsx-archive <change-name>
   → Commit the archive

   Open a PR to main when ready.

### Phase 2a — Propose the change (15 min)

Create a new OpenSpec change:

```
/opsx-propose mysql-migration
```

When prompted, describe the change:
> "Migrate the Employee persistence layer from in-memory HashMap to MySQL using JPA. Add @Entity to Employee, create a JPA repository, wire it into the service, add datasource config, and write integration tests with Testcontainers."

Review the generated artifacts:
- `openspec/changes/mysql-migration/proposal.md` — what and why
- `openspec/changes/mysql-migration/design.md` — how (JPA, repository pattern, Testcontainers)
- `openspec/changes/mysql-migration/tasks.md` — step-by-step implementation

Edit the artifacts if needed. The proposal is the contract between you and the AI.

### Phase 2b — Implement the change (40 min)

```
/opsx-apply mysql-migration
```

opencode works through the tasks. The key implementation steps:

**JPA setup:**
- Add `@Entity` and `@Id @GeneratedValue` to `Employee`
- Create `EmployeeJpaRepository extends JpaRepository<Employee, Long>`
- Update `EmployeeService` to use the JPA repository
- Add `application.properties` datasource config

Dependencies already in `build.gradle` (uncomment when ready):
```
spring-boot-starter-data-jpa
mysql-connector-j
```

**Integration tests:**

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

### Phase 2c — Archive (5 min)

Once tests are green:

```
/opsx-archive mysql-migration
```

The change moves to `openspec/changes/archive/`. The artifacts are preserved — AI can reference them in future sessions.

### Debrief (15 min)

Questions:
- "What did opencode get right in the proposal on the first pass? What did you correct?"
- "What happened when you changed a task description and re-ran apply?"
- "What does the compiler error tell you that a test wouldn't?"

**Points to land:**
- The proposal is now the source of truth. The compiler enforces the contract, not discipline.
- opencode wrote the implementation correctly *because* the tasks were precise.
- The test verifies behaviour against a real database — the proposal verified the approach.

---

## Act 3 — DynamoDB Migration with OpenSpec (35 min)

### Goal

Use the archived MySQL change as context to drive a second persistence migration — this time to DynamoDB.

### Phase 3a — Propose the change (10 min)

```
/opsx-propose dynamodb-migration
```

Describe the change:
> "Migrate the Employee persistence layer from MySQL/JPA to DynamoDB using the AWS SDK v2. Use LocalStack for local development. Replace EmployeeJpaRepository with a DynamoDbEmployeeRepository. Keep all controller and service code unchanged. Update tests to use LocalStackContainer."

opencode can read the archived `mysql-migration` change for context — it knows what was done before and why.

### Phase 3b — Implement the change (20 min)

```
/opsx-apply dynamodb-migration
```

Key implementation steps:

- Remove `spring-boot-starter-data-jpa` and `mysql-connector-j` from `build.gradle`
- Add `software.amazon.awssdk:dynamodb` and `org.testcontainers:localstack`
- Create `DynamoDbEmployeeRepository` implementing the same `EmployeeRepository` interface
- Update `EmployeeControllerIT` to use `LocalStackContainer`

```java
@Container
static LocalStackContainer localstack =
    new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(DYNAMODB);
```

```bash
./gradlew test
```

Same tests. Same interface. Different database. Green.

### Phase 3c — Archive (5 min)

```
/opsx-archive dynamodb-migration
```

### The point

> "opencode didn't need to re-understand the codebase. The archived MySQL change told it what the persistence layer looked like. It only had to solve the DynamoDB problem — which is what we actually wanted."

---

## Final Discussion (15 min)

Three questions:

1. **"When would you still vibe-code?"**
   Let them draw the line. Prototypes, throwaway scripts, one-consumer internal tools are reasonable answers.

2. **"Who owns the specs in your team?"**
   The specs live in `openspec/`, versioned in git. Who writes the first draft? Who approves changes? This is a process question.

3. **"What changes about how you prompt opencode going forward?"**
   Expected landing: lead with a structured change, not with a description. The proposal is the most precise prompt you can give for any infrastructure task.
