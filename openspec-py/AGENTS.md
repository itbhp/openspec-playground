# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Three acts:
- **Act 1** (vibe coding): Write tests that would catch a broken persistence layer.
- **Act 2** (spec v1): Write MySQL via SQLAlchemy
- **Act 3** (spec v2): Refactor MySQL → DynamoDB using the spec as "persistent memory"

Domain: Employee CRUD REST API. Persistence changes, domain stays the same.

Stack: Python 3.12, FastAPI, SQLAlchemy 2.0, Uvicorn, Docker Compose.

## Commands

```bash
docker compose up -d                          # Start all services (api, mysql, localstack)
docker compose exec api pytest                # Run tests
docker compose exec api pip install <pkg>     # Add a dependency (then update requirements.txt)
docker compose logs -f api                    # Tail app logs
```

All commands run inside Docker containers. The dev server runs with --reload.

## Architecture

- Model: `Employee` dataclass (plain fields — no SQLAlchemy mapping yet)
- Repository: Custom `EmployeeRepository` ABC, not a SQLAlchemy session. Decoupled for
  clean phase transitions.
- Impl: `InMemoryEmployeeRepository` (dict + counter)
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeeController` APIRouter at `/employees` with full CRUD
- Wiring: `app/dependencies.py` (FastAPI Depends) — swap the repository here

## Testing

Zero tests currently. Test tooling pre-staged: pytest + httpx.
Docker Compose services: `mysql:8.0`, `localstack/localstack`.
Test dir exists but is empty: `tests/`

## Conventions

- Plain dataclass model — no ORM decorators until Act 2
- Pydantic only at the HTTP boundary (EmployeeIn/EmployeeOut)
- Custom repository ABC (not a SQLAlchemy session) to keep persistence decoupled
- Swap the `get_employee_repository` binding in `app/dependencies.py` during migrations
- SQLAlchemy is installed and configured, ready for Act 2
- `.env` has `DATABASE_URL` pointing to the MySQL compose service
- Container runs as the host user (UID/GID mapped via Docker build args)

