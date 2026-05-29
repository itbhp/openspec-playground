## Why

The project has zero CI — no automated build or test verification on push/PR. A basic GitHub Actions workflow ensures code compiles and tests pass before merging, establishing quality gating for all future changes.

## What Changes

- Add `.github/workflows/build.yml` with a CI workflow triggered on `push` and `pull_request` to `main`
- Workflow checks out code, sets up JDK 21, runs `./gradlew build` (compile + test + package)
- No deployment, no artifact publishing — just build + test verification

## Capabilities

### New Capabilities
- `ci-build`: Continuous integration workflow for building and testing the project on GitHub Actions

### Modified Capabilities

(No existing capabilities to modify.)

## Impact

- New file: `.github/workflows/build.yml`
- No changes to application code, build config, or dependencies
