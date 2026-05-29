## 1. Create GitHub Actions workflow directory

- [x] 1.1 Create `.github/workflows/` directory in project root

## 2. Create CI workflow file

- [x] 2.1 Create `.github/workflows/build.yml` with workflow name `build`
- [x] 2.2 Add `on` triggers: `push` and `pull_request` on `main` branch
- [x] 2.3 Add `jobs.build` with `runs-on: ubuntu-latest`
- [x] 2.4 Add step: `actions/checkout@v4`
- [x] 2.5 Add step: `actions/setup-java@v4` with Java 21 and Eclipse Temurin distribution
- [x] 2.6 Add step: Run `./gradlew build` with Gradle wrapper
- [x] 2.7 Grant execute permission on `gradlew` before running

## 3. Verify workflow

- [x] 3.1 Confirm `.github/workflows/build.yml` is valid YAML
- [x] 3.2 Run `./gradlew build` locally still works
