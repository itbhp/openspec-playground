## MODIFIED Requirements

### Requirement: Durable storage
Employee data SHALL persist across application restarts when the active `EmployeeRepository` implementation is backed by a durable store (any non-in-memory implementation, e.g. MySQL or DynamoDB).

#### Scenario: Data survives a restart
- **WHEN** an employee is created and the application is subsequently restarted against the same durable backing store
- **THEN** the employee SHALL still be retrievable via `findAll()` or `findById()` after restart
