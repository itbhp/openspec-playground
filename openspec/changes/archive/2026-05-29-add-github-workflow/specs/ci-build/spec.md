## ADDED Requirements

### Requirement: CI workflow exists
The repository SHALL contain a GitHub Actions workflow at `.github/workflows/build.yml` that is triggered on `push` and `pull_request` events for the `main` branch.

#### Scenario: Workflow triggers on push to main
- **WHEN** a commit is pushed to the `main` branch
- **THEN** the CI workflow SHALL start running

#### Scenario: Workflow triggers on PR to main
- **WHEN** a pull request is opened or updated targeting `main`
- **THEN** the CI workflow SHALL start running

### Requirement: Build and test execution
The workflow SHALL check out the repository, set up JDK 21 (Eclipse Temurin), and run `./gradlew build` which compiles the code and executes all tests.

#### Scenario: Successful build
- **WHEN** the workflow runs `./gradlew build`
- **AND** all code compiles and all tests pass
- **THEN** the workflow SHALL exit with status 0

#### Scenario: Compilation failure
- **WHEN** the workflow runs `./gradlew build`
- **AND** the code fails to compile
- **THEN** the workflow SHALL exit with non-zero status
- **AND** SHALL mark the workflow run as failed

#### Scenario: Test failure
- **WHEN** the workflow runs `./gradlew build`
- **AND** compilation succeeds but tests fail
- **THEN** the workflow SHALL exit with non-zero status
- **AND** SHALL mark the workflow run as failed
