# Presenter Guide — Spec-Driven Persistence Migrations

**Duration:** Full day (~6h) · **Stack:** Java · Spring Boot · Gradle · Testcontainers

This guide contains the presenter's script, debrief questions, verification checklists, and talking points. It is **not** for participants.

---

## Schedule (flow, not fixed times)

| Segment | Notes |
|---------|-------|
| Framing talk | 20 min — no hands-on |
| **Act 1** — Vibe coding | Let them work. Let them flounder. |
| Debrief Act 1 | Compare outputs across tables — chaos is the point |
| **Act 2** — MySQL with OpenSpec (Propose) | `/opsx-propose`, review, refine |
| **Act 2** — MySQL with OpenSpec (Apply) | Main work block. Buffer for comp issues. |
| Break | Let Docker rest |
| Debrief Act 2 | What got caught in spec review? |
| **Act 3** — DynamoDB with OpenSpec (Propose) | Quick — archive carries most of the context |
| **Act 3** — DynamoDB with OpenSpec (Apply) | LocalStack, DynamoDB SDK, test updates |
| Debrief Act 3 | UUID debate resurfaces with DynamoDB context |
| Open floor retry | "Now retry Act 2 with vibe coding — no OpenSpec" |
| Final discussion | Contrast, team ownership of specs |

---

## Framing Talk (20 min)

No hands-on. Cover:

- What "vibe coding" means in practice: prompt → working code → ship
- What spec-driven development means: define the change formally, let AI implement against it
- Why the distinction matters for backend teams: when you change infrastructure, you need precision — not hoping the AI remembers or guesses what the code does
- The two claims you'll prove today:
  1. A structured change makes AI output more predictable and correct
  2. Captured specs act as persistent memory — AI can drive non-trivial refactors across sessions without losing context
- Quick demo of the OpenSpec workflow: `/opsx-propose` → review artifacts → `/opsx-apply`

---

## Act 1 — Vibe Coding

### Walk through the skeleton (5 min)

- `Employee.java` — the POJO, fully written
- `EmployeeController.java` — all five operations stubbed, in-memory persistence
- `EmployeeService.java` — thin pass-through to `EmployeeRepository`
- `InMemoryEmployeeRepository.java` — `ConcurrentHashMap` + `AtomicLong`
- No tests exist

Everything compiles and runs. `GET /employees` returns an empty list.

### The exercise

> "Use opencode to write tests that would catch a broken persistence layer. You have 25 minutes."

No further instructions. Do NOT clarify. Let them decide what "catch a broken persistence layer" means.

### While walking the room, watch for

- Are they writing unit tests (mocks) or integration tests (`@SpringBootTest`)?
- Are they testing at the HTTP boundary or the service boundary?
- Are they covering error paths (404, 204) or only happy paths?
- Are they using `TestRestTemplate` or `MockMvc`?
- Are tests truly independent or do they share state?

### Debrief (15 min)

- "What did you write — unit tests with mocks, or integration tests against the running app?"
- "If we swapped `InMemoryEmployeeRepository` for a broken implementation, would your tests fail?"
- "Compare your test structure with your neighbour's. Same coverage? Same assertions? Same approach?"
- "Would two teams in your organisation produce the same tests for the same problem?"

**Point to land:** Fast, capable, inconsistent. Every run produces a different testing strategy. When the persistence layer changes in Act 2 and Act 3, tests written this way will break — or worse, silently pass. The AI needs a structured way to work. That's what OpenSpec provides.

---

## Act 2 — MySQL Migration with OpenSpec

### Goal

Use OpenSpec to structure a persistence migration from in-memory to MySQL, implement it, and verify with Testcontainers.

### Phase 2a — Propose (let them work, then review as a group)

The prompt participants use:
> "Migrate the Employee persistence layer from in-memory HashMap to MySQL using JPA. Add @Entity to Employee, create a JPA repository implementing the existing EmployeeRepository interface, wire it into the service via Spring's bean mechanism, add datasource config, and write integration tests using Testcontainers MySQLContainer with @DynamicPropertySource. The EmployeeRepository interface and all service/controller code must remain unchanged."

**Key things to verify in their proposals:**
- `Employee` gets `@Entity`, `@Id`, `@GeneratedValue` — and nothing else changes (no `@Table`, no `@Column`)
- New `EmployeeJpaRepository extends JpaRepository<Employee, Long>` is created
- A new `JpaEmployeeRepository` adapter implements `EmployeeRepository` and delegates to `EmployeeJpaRepository`
- `EmployeeController` and `EmployeeService` are in the "do not touch" list
- JPA and MySQL dependencies in `build.gradle` are uncommented
- `application.properties` datasource config is uncommented, including `spring.jpa.hibernate.ddl-auto=update`
- `InMemoryEmployeeRepository` has `@Repository` removed (bean conflict with two `EmployeeRepository` impls)
- any change to `openspec/config.yaml`? (Active persistence: mysql)

### Phase 2b — Apply (let them work, walk the room)

**Key things to watch for during implementation:**
- Did the AI accidentally touch `EmployeeService` or `EmployeeController`?
- Does `JpaEmployeeRepository` throw at compile time if a method is missing from `EmployeeRepository`?
- Two `@Repository` beans of type `EmployeeRepository` → `JpaEmployeeRepository` takes priority? Or does Spring fail with bean ambiguity?
- Are the tests at HTTP level (`TestRestTemplate`, `RANDOM_PORT`) or unit tests?
- Is `@DirtiesContext` used unnecessarily? (slows down tests)

### Phase 2c — Archive

```bash
/opsx-archive mysql-migration
```

Commit. Open a PR to main.

### Debrief (15 min)

- "What did opencode get right in the proposal on the first pass? What did you correct?"
- "Did `EmployeeController` or `EmployeeService` change? Why not?"
- "What does the compiler tell you if the JPA adapter doesn't correctly implement `EmployeeRepository`?"
- "If you ran this same prompt tomorrow, would you get the same output?"

**Points to land:**
- The `EmployeeRepository` interface is the contract. Persistence is an implementation detail behind it. The compiler enforced this — not discipline.
- The proposal is now archived. The next change can build on it without re-explaining the codebase.
- If any two participants got different proposals, that's the inconsistency problem — OpenSpec reduces it but doesn't eliminate it. Review matters.

---

## Act 3 — DynamoDB Migration with OpenSpec

### Goal

Use the archived MySQL change as context to drive a second persistence migration — to DynamoDB on LocalStack.

### Phase 3a — Propose (10 min)

The prompt participants use:
> "Migrate the Employee persistence layer from MySQL/JPA to DynamoDB using the AWS SDK v2. Use LocalStack for local development and testing. Replace the JPA implementation with a DynamoDbEmployeeRepository that implements the existing EmployeeRepository interface. Remove JPA and MySQL dependencies. Update integration tests to use LocalStackContainer. Keep all controller and service code unchanged."

**Key things to verify in their proposals:**
- `spring-boot-starter-data-jpa` and `mysql-connector-j` removed from `build.gradle`
- `DynamoDbEmployeeRepository` implements `EmployeeRepository`
- `EmployeeControllerIT` updated to use `LocalStackContainer` with `DYNAMODB` service
- ID generation strategy — does the proposal address it? `Long` stays (AtomicLong) or migrates to `String` (UUID)?
- Does the proposal flag the hot partition concern with sequential `Long` keys?

**If the UUID debate doesn't surface naturally:**

Ask the two staff engineers: *"Long IDs as DynamoDB partition keys — any concerns?"* That should spark it. If it doesn't, add yourself: *"Sequential partition keys can hot-spot on the last physical partition. Is that a problem here? What would you change?"*

The debate is intentional. The point is not to resolve it — it's to surface that the spec review is where these tradeoffs get caught, not in broken code.

### Phase 3b — Apply (let them work, walk the room)

**Key things to watch for:**
- `DynamoDbClient` bean configuration — is it using the enhanced client or low-level? Either is fine, but must be consistent.
- Table creation — is there a `@PostConstruct` or `ApplicationRunner` that calls `CreateTableRequest`?
- LocalStack endpoint — is it using `localstack.getEndpointOverride(DYNAMODB)` or hardcoded `localhost:4566`? The former is correct for containers.
- Credentials — LocalStack needs dummy credentials, not the default chain.
- `Employee` annotations — JPA annotations stripped? `@DynamoDbBean` added (if enhanced client) or left bare (if low-level)?
- Tests — same 5 test methods, same assertions, different container setup.

### Phase 3c — Archive

```
/opsx-archive dynamodb-migration
```

### Debrief (10 min)

- "How much of the DynamoDB implementation did opencode get right on the first pass?"
- "Did you need to read the source code of the MySQL implementation, or was the archive enough?"
- "If you were doing this without OpenSpec, how would you know what the MySQL adapter looked like?"
- "What decisions did your team need to make that the AI couldn't — and that the spec-review caught?"

**The point:**

> "opencode didn't need to re-understand the codebase. The archived MySQL change gave it the full context of what was built and why. It only had to solve the DynamoDB problem."

---

## Open Floor Retry

> "Now retry Act 2 — the same MySQL migration — using vibe coding only. No `/opsx-propose`. No OpenSpec. Just a prompt to the AI. 15 minutes."

This is the hammer. They will feel the absence of the spec. They will watch the AI guess the architecture, make different naming choices, produce different test structures.

If they ask "how do I know what was done before", the answer is: *you don't. That's the problem.*

---

## Final Discussion

1. **"When would you still vibe-code?"**
   Prototypes, throwaway scripts, one-consumer internal tools. Let them draw the line.

2. **"Who owns the specs in your team?"**
   The specs live in `openspec/`, versioned in git, reviewed in PRs. Who writes the first draft? Who approves changes?

3. **"What changes about how you prompt opencode going forward?"**
   Expected landing: lead with a structured change. The proposal is the most precise context you can give AI for any infrastructure task.

4. **"Would you do Act 3 differently if you were starting from scratch?"**
   (This is the meta question — they now have the full arc.)

5. **"What happens if someone refactors the contract without updating the specs?"**
   The code is the runtime truth. The spec is the design-time truth. **A PR that changes code but not the spec is incomplete.**

---

## Notes for the presenter

- The `openspec/` directory and `config.yaml` are pre-configured in the repo. Participants should not need to create them.
- MySQL and LocalStack images should be pre-pulled by participants (listed in the README prerequisites).
- If `./gradlew build` takes >2 minutes during an apply phase, participants can run `./gradlew test` for a faster feedback loop once compilation is confirmed.
- The UUID/Long debate in Act 3 is the best "trap" in the workshop. If it doesn't surface naturally, prompt it. The debate itself is the teaching moment — not the resolution.
