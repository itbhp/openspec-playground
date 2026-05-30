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
- Your usual editor/IDE
- Pre-pull Docker images before the session to avoid cold-pull delays:

```bash
docker pull mysql:8.0
docker pull localstack/localstack
```

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
   1. A structured change makes AI output more predictable and correct
   2. Captured specs act as persistent memory — AI can drive non-trivial refactors across sessions without losing context
- Quick demo of the OpenSpec workflow: `/opsx-propose` → review artifacts → `/opsx-apply`
- Point to `openspec/changes/archive/2026-05-29-add-github-workflow/` as a concrete example of what a completed change looks like — proposal, design, tasks, spec, archive

---

## Act 1 — Vibe Coding (30 min)

### Goal

Experience what AI-assisted development looks like with no structured change.

### Setup

Walk through the skeleton briefly (5 min):
- `Employee.java` — the POJO, fully written
- `EmployeeController.java` — all five operations stubbed, in-memory persistence
- `EmployeeService.java` — thin pass-through to `EmployeeRepository`
- `InMemoryEmployeeRepository.java` — `ConcurrentHashMap` + `AtomicLong`
- No tests exist

Everything compiles and runs. `GET /employees` returns an empty list.

### The exercise

> "Use opencode to write tests that would catch a broken persistence layer. You have 25 minutes."

No further instructions. Let them decide what "catch a broken persistence layer" means to them.

### Debrief (15 min)

- "What did you write — unit tests with mocks, or integration tests against the running app?"
- "If we swapped `InMemoryEmployeeRepository` for a broken implementation, would your tests fail?"
- "Compare your test structure with your neighbour's. Same coverage? Same assertions? Same approach?"

**Point to land:** Fast, capable, inconsistent. Every run produces a different testing strategy. When the persistence layer changes in Act 2 and Act 3, tests written this way will break — or worse, silently pass. The AI needs a structured way to work. That's what OpenSpec provides.

---

## Act 2 — MySQL Migration with OpenSpec (80 min)

### Goal

Use OpenSpec to structure a persistence migration from in-memory to MySQL, implement it, and verify with Testcontainers.

### The OpenSpec Workflow

Follow these steps in order. Do not skip ahead.

1. **Create a branch** from main
```bash
git checkout -b mysql-migration
```

2. **Propose** — describe what you want to change
```
/opsx-propose mysql-migration
```
Review the generated artifacts. Edit if needed. Commit them.

3. **Apply** — let AI implement the tasks
```
/opsx-apply mysql-migration
```
Review each task's output. Run `./gradlew build`. Commit when green.

4. **Archive** — finalize the change
```
/opsx-archive mysql-migration
```
Commit the archive. Open a PR to main.

### Phase 2a — Propose the change (15 min)

```
/opsx-propose mysql-migration
```

When prompted, describe the change:
> "Migrate the Employee persistence layer from in-memory HashMap to MySQL using JPA. Add @Entity to Employee, create a JPA repository implementing the existing EmployeeRepository interface, wire it into the service via Spring's bean mechanism, add datasource config, and write integration tests using Testcontainers MySQLContainer with @DynamicPropertySource. The EmployeeRepository interface and all service/controller code must remain unchanged."

Review the generated artifacts:
- `openspec/changes/mysql-migration/proposal.md`
- `openspec/changes/mysql-migration/design.md`
- `openspec/changes/mysql-migration/tasks.md`

**Note**: any change to `openspec/config.yaml`? (hint  Active persistence: mysql)

**Read them.** You are the author. opencode is the typist. If anything is wrong or missing, edit before moving on. Commit the artifacts.

### Phase 2b — Implement the change (50 min)

```
/opsx-apply mysql-migration
```

opencode works through the tasks. Key things to verify as it goes:

- `Employee` gets `@Entity`, `@Id`, `@GeneratedValue` — and nothing else changes
- New `EmployeeJpaRepository extends JpaRepository<Employee, Long>` is created
- A new `JpaEmployeeRepository` adapter implements `EmployeeRepository` and delegates to `EmployeeJpaRepository`
- `EmployeeController` and `EmployeeService` are **not touched**
- JPA and MySQL dependencies in `build.gradle` are uncommented
- `application.properties` datasource config is uncommented

Integration tests must cover at minimum:

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

    @Test void createAndRetrieve() { ... }
    @Test void getUnknownReturns404() { ... }
    @Test void deleteAndConfirmGone() { ... }
}
```

```bash
./gradlew test
```

### Phase 2c — Archive (5 min)

Once tests are green:

```
/opsx-archive mysql-migration
```

Commit. Open a PR to main.

### Debrief (15 min)

- "What did opencode get right in the proposal on the first pass? What did you correct?"
- "Did `EmployeeController` or `EmployeeService` change? Why not?"
- "What does the compiler tell you if the JPA adapter doesn't correctly implement `EmployeeRepository`?"

**Points to land:**
- The `EmployeeRepository` interface is the contract. Persistence is an implementation detail behind it. The compiler enforced this — not discipline.
- The proposal is now archived. The next change can build on it without re-explaining the codebase.

---

## Act 3 — DynamoDB Migration with OpenSpec (35 min)

### Goal

Use the archived MySQL change as context to drive a second persistence migration — to DynamoDB on LocalStack.

### Phase 3a — Propose the change (10 min)

```
/opsx-propose dynamodb-migration
```

Describe the change:
> "Migrate the Employee persistence layer from MySQL/JPA to DynamoDB using the AWS SDK v2. Use LocalStack for local development and testing. Replace the JPA implementation with a DynamoDbEmployeeRepository that implements the existing EmployeeRepository interface. Remove JPA and MySQL dependencies. Update integration tests to use LocalStackContainer. Keep all controller and service code unchanged."

Review the proposal carefully before accepting. This is the moment to catch anything the AI gets wrong — or anything it gets interestingly right.

### Phase 3b — Implement the change (20 min)

```
/opsx-apply dynamodb-migration
```

Key things to verify:

- `spring-boot-starter-data-jpa` and `mysql-connector-j` removed from `build.gradle`
- `software.amazon.awssdk:dynamodb` and `org.testcontainers:localstack` added
- `DynamoDbEmployeeRepository` implements `EmployeeRepository`
- `EmployeeControllerIT` updated to use `LocalStackContainer`:

```java
@Container
static LocalStackContainer localstack =
    new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(DYNAMODB);
```

```bash
./gradlew test
```

Same tests. Same `EmployeeRepository` interface. Different database.

### Phase 3c — Archive (5 min)

```
/opsx-archive dynamodb-migration
```

### The point

> "opencode didn't need to re-understand the codebase. The archived MySQL change gave it the full context of what was built and why. It only had to solve the DynamoDB problem."

---

## Final Discussion (15 min)

1. **"When would you still vibe-code?"**
   Prototypes, throwaway scripts, one-consumer internal tools. Let them draw the line.

2. **"Who owns the specs in your team?"**
   The specs live in `openspec/`, versioned in git, reviewed in PRs. Who writes the first draft? Who approves changes?

3. **"What changes about how you prompt opencode going forward?"**
   Expected landing: lead with a structured change. The proposal is the most precise context you can give AI for any infrastructure task.