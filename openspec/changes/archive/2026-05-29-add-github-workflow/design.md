## Context

The project is a Java 21 / Spring Boot 3.4.5 / Gradle 9.3 project with Testcontainers dependencies already staged. Currently there is no CI — builds and tests run only locally. A GitHub Actions workflow is the standard approach for zero-cost CI on GitHub-hosted repositories.

## Goals / Non-Goals

**Goals:**
- Add a GitHub Actions workflow that builds and tests the project on every push and PR to `main`
- Use JDK 21 (matches project target)
- Fail fast — any compile error or test failure blocks merging

**Non-Goals:**
- No deployment or artifact publishing
- No multi-OS matrix builds
- No test result reporting or code coverage
- No caching of Gradle dependencies (can be added later)

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| CI platform | GitHub Actions | Already hosted on GitHub; zero-config, no additional accounts |
| Trigger events | `push` + `pull_request` on `main` | Standard branch protection — PRs get validated, direct pushes also checked |
| Runner | `ubuntu-latest` | Matches local dev environment; no macOS/Windows needed |
| Java distro | `temurin` (Eclipse Adoptium) | Widely used, LTS-compatible, available on GitHub Actions |
| Build command | `./gradlew build` | Runs compile + test + package in one step; leverages existing Gradle setup |
| Gradle wrapper | Used directly (via `gradle/wrapper` checked in) | Standard approach — no pre-installed Gradle version needed |

## Risks / Trade-offs

- **Gradle daemon startup time** → Each workflow run starts cold; ~1-2 min overhead. Acceptable for a simple build.
- **No Gradle caching** → Dependency downloads on every run adds time. Mitigation: add caching when build times become noticeable (>3 min).
- **No Docker-in-Docker** → If Testcontainers tests need Docker, the `ubuntu-latest` runner has Docker pre-installed, so no risk.
