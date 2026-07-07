# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Half-day format (3.5h), three acts:
- **Act 1** (vibe coding): Write tests that would catch a broken persistence layer.
- **Act 2** (spec v1): Write MySQL via Doctrine ORM
- **Act 3** (spec v2): Refactor MySQL → DynamoDB using the spec as "persistent memory"

Domain: Employee CRUD REST API. Persistence changes, domain stays the same.

Stack: PHP 8.5, Symfony 8.1, Doctrine ORM, FrankenPHP 1.12, Docker Compose.

## Commands

```bash
docker compose up -d          # Start all services (PHP, MySQL, LocalStack)
docker compose exec php bin/console server:dump  # Debug
docker compose exec php php bin/phpunit          # Run tests
docker compose exec php composer install         # Install dependencies
```

No CI, no Makefile. All commands run inside Docker containers.

## Architecture

- Model: `Employee` POJO (mutable, plain getters/setters — intentional for ORM compat in later phases)
- Repository: Custom `EmployeeRepositoryInterface`, not Doctrine's `ServiceEntityRepository`. Decoupled from persistence tech for clean phase transitions.
- Impl: `InMemoryEmployeeRepository` (array + int counter)
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeeController` at `/employees` with full CRUD

Namespace: `App\`

## Testing

Zero tests currently. Test dependencies pre-staged:
- PHPUnit (via `symfony/test-pack`)
- Docker Compose services: `mysql:8.0`, `localstack/localstack`

Test dir exists but is empty: `tests/`

## Conventions

- No readonly properties on model — plain PHP class with getters/setters
- Custom repository interface (not Doctrine repository) to keep persistence tech decoupled
- ORM mapping strategy (attributes vs XML) decided by participants during the kata
- Doctrine ORM is installed and configured, ready for Act 2
- `.env` has `DATABASE_URL` pointing to the MySQL compose service
- The `compose.yaml` includes MySQL and LocalStack services, ready for Act 2 and Act 3
- Interface binding in `config/services.yaml` — swap implementation there during migrations

