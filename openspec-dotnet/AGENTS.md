# AGENTS.md

## Project

Workshop project for teaching **spec-driven development vs vibe coding**. Three acts:
- **Act 1** (vibe coding): Write tests that would catch a broken persistence layer.
- **Act 2** (spec v1): Write MySQL via Entity Framework Core
- **Act 3** (spec v2): Refactor MySQL → DynamoDB using the spec as "persistent memory"

Domain: Employee CRUD REST API. Persistence changes, domain stays the same.

Stack: .NET 8, ASP.NET Core Web API, EF Core 8 (Pomelo MySQL), Docker Compose.

## Commands

```bash
docker compose up -d                              # Start all services (api, mysql, localstack)
docker compose exec api dotnet test                # Run tests
docker compose exec api dotnet add package <pkg>   # Add a NuGet package
docker compose logs -f api                         # Tail app logs
```

All commands run inside Docker containers. The dev server runs with `dotnet watch`.

## Architecture

- Model: `Employee` POCO (plain properties — no EF Core attributes yet)
- Repository: Custom `IEmployeeRepository` interface, not a `DbContext`. Decoupled for
  clean phase transitions.
- Impl: `InMemoryEmployeeRepository` (`Dictionary` + counter), registered as Singleton
- Service: Thin CRUD pass-through to repository
- Controller: `EmployeesController` at `/employees` with full CRUD
- Wiring: `Program.cs` (built-in DI) — swap the `IEmployeeRepository` registration here

## Testing

Zero tests currently. Docker Compose services: `mysql:8.0`, `localstack/localstack`.
Test dir exists but is empty: `Tests/`

## Conventions

- Plain POCO model — no EF attributes until Act 2
- `EmployeeDto` record only at the HTTP boundary
- Custom repository interface (not a DbContext) to keep persistence decoupled
- In-memory repo registered as Singleton so state survives between requests
- Swap the `IEmployeeRepository` registration in `Program.cs` during migrations
- EF Core + Pomelo installed and configured, ready for Act 2
- `.env` / `ConnectionStrings__Default` points to the MySQL compose service
- Container runs as the host user (UID/GID mapped via Docker build args)

