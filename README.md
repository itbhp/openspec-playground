# OpenSpec Workshop: Spec-Driven Persistence Migrations

**Duration:** Full day · **Stack:** Java · Spring Boot · Gradle · Testcontainers

---

## What is OpenSpec?

[OpenSpec](https://openspec.dev) is a CLI tool and set of opencode skills that bring structure to AI-assisted development. Instead of ad-hoc prompts, you work through a defined workflow: **explore** the problem, **propose** a change (producing a proposal, design, and task list), **apply** the tasks, and **archive** the completed change. 

The specs and decisions are captured in files under `openspec/`, giving AI persistent context across sessions.

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

## Act 1 — Vibe Coding

### Goal

Experience what AI-assisted development looks like with no structured change.

### Setup

The skeleton (your presenter will walk through it):

- `Employee.java` — the POJO, fully written
- `EmployeeController.java` — all five operations stubbed, in-memory persistence
- `EmployeeService.java` — thin pass-through to `EmployeeRepository`
- `InMemoryEmployeeRepository.java` — `ConcurrentHashMap` + `AtomicLong`
- No tests exist

Everything compiles and runs. `GET /employees` returns an empty list.

### The exercise

> "Use opencode to write tests that would catch a broken persistence layer."

No further instructions. Decide what "catch a broken persistence layer" means to you.

---

## Act 2 — MySQL Migration with OpenSpec

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

Describe the change like this:

> "Migrate the Employee persistence layer from in-memory HashMap to MySQL using JPA. Add @Entity to Employee, create a JPA repository implementing the existing EmployeeRepository interface, wire it into the service via Spring's bean mechanism, add datasource config, and write integration tests using Testcontainers MySQLContainer with @DynamicPropertySource. The EmployeeRepository interface and all service/controller code must remain unchanged."

Review the generated artifacts:
- `openspec/changes/mysql-migration/proposal.md`
- `openspec/changes/mysql-migration/design.md`
- `openspec/changes/mysql-migration/tasks.md`

**Note**: any change to `openspec/config.yaml`? (hint → Active persistence: mysql)

**Read them.** You are the author. opencode is the typist. If anything is wrong or missing, edit before moving on. Commit the artifacts.

3. **Apply** — let AI implement the tasks
```
/opsx-apply mysql-migration
```

Review each task's output. Run `./gradlew build`. Commit when green.

Integration tests will look like this:

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

4. **Archive** — finalize the change
```
/opsx-archive mysql-migration
```

Commit the archive. Open a PR to main.

---

## Act 3 — DynamoDB Migration with OpenSpec

### Goal

Use the archived MySQL change as context to drive a second persistence migration — to DynamoDB on LocalStack.

### Phase 3a — Propose the change

```
/opsx-propose dynamodb-migration
```

Describe the change:

> "Migrate the Employee persistence layer from MySQL/JPA to DynamoDB using the AWS SDK v2. Use LocalStack for local development and testing. Replace the JPA implementation with a DynamoDbEmployeeRepository that implements the existing EmployeeRepository interface. Remove JPA and MySQL dependencies. Update integration tests to use LocalStackContainer. Keep all controller and service code unchanged."

Review the proposal carefully before accepting. This is the moment to catch anything the AI gets wrong — or anything it gets interestingly right.

### Phase 3b — Implement the change

```
/opsx-apply dynamodb-migration
```

```bash
./gradlew test
```

### Phase 3c — Archive

```
/opsx-archive dynamodb-migration
```

Same tests. Same `EmployeeRepository` interface. Different database.
