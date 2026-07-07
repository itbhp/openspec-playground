# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Three acts:
- **Act 1** (vibe coding): Write tests that would catch a broken persistence layer.
- **Act 2** (spec v1): Write MySQL via MikroORM
- **Act 3** (spec v2): Refactor MySQL → DynamoDB using the spec as "persistent memory"

Domain: Employee CRUD REST API. Persistence changes, domain stays the same.

Stack: Node.js 22, NestJS 11, MikroORM 6, TypeScript 5, Docker Compose.

## Commands

```bash
docker compose up -d                        # Start all services (node, mysql, localstack)
docker compose exec node npm run test       # Run unit tests
docker compose exec node npm run test:e2e   # Run integration tests
docker compose exec node npm install <pkg>  # Add a dependency
docker compose logs -f node                 # Tail app logs
```

All commands run inside Docker containers. The dev server runs in watch mode (`start:dev`).

## Architecture

- Model: `Employee` plain class (mutable public fields — intentional, no ORM decorators
  yet for MikroORM compat in later phases)
- Repository: Custom `EmployeeRepository` interface bound via the `EMPLOYEE_REPOSITORY`
  DI token, not a MikroORM `EntityRepository`. Decoupled from persistence tech for clean
  phase transitions.
- Impl: `InMemoryEmployeeRepository` (`Map` + counter)
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeeController` at `/employees` with full CRUD

Module: `AppModule` in `src/app.module.ts`

## Testing

Zero tests currently. Test tooling pre-staged: Jest + Supertest (via `@nestjs/testing`).
Docker Compose services: `mysql:8.0`, `localstack/localstack`.
Test dir exists but is empty: `test/`

## Conventions

- No decorators on the model — plain class with public fields
- Custom repository interface (not MikroORM repository) to keep persistence tech decoupled
- Swap the `EMPLOYEE_REPOSITORY` binding in `src/app.module.ts` during migrations — this
  is the only wiring change a persistence migration should need
- MikroORM is installed and configured, ready for Act 2
- `.env` has `DATABASE_URL` pointing to the MySQL compose service
- The `compose.yaml` includes MySQL and LocalStack services, ready for Act 2 and Act 3
- Container runs as the host user (UID/GID mapped via Docker build args)

