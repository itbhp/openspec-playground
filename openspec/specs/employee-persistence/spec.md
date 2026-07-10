# employee-persistence Specification

## Purpose
TBD - created by archiving change mysql-migration. Update Purpose after archive.
## Requirements
### Requirement: Storage-agnostic repository contract
Every `EmployeeRepository` implementation SHALL provide identical CRUD behavior regardless of the backing store, so the service and controller layers behave the same whether employees are stored in memory or in MySQL.

#### Scenario: Listing employees when none exist
- **WHEN** `findAll()` is called on an empty store
- **THEN** it SHALL return an empty list, not `null`

#### Scenario: Listing all saved employees
- **WHEN** multiple employees have been saved and `findAll()` is called
- **THEN** it SHALL return all of them

#### Scenario: Snapshot isolation of findAll results
- **WHEN** the list returned by `findAll()` is held by the caller and a new employee is saved afterward
- **THEN** the previously returned list SHALL NOT reflect the new employee

### Requirement: Identity assignment on save
The repository SHALL assign a unique id to a new employee and SHALL update an existing employee in place when a save is given an id that already exists.

#### Scenario: New employee receives a generated id
- **WHEN** `save()` is called with an employee that has no id
- **THEN** the returned employee SHALL have a non-null id

#### Scenario: Successive new employees receive distinct ids
- **WHEN** two employees with no id are saved one after another
- **THEN** their assigned ids SHALL be different

#### Scenario: Saving with an existing id updates in place
- **WHEN** `save()` is called with an employee whose id matches a previously saved employee
- **THEN** the stored employee's fields SHALL be replaced with the new values
- **AND** no additional employee record SHALL be created

### Requirement: Lookup by id
The repository SHALL support retrieving a single employee by id, distinguishing between found and not-found.

#### Scenario: Employee found by id
- **WHEN** `findById()` is called with an id that was previously saved
- **THEN** it SHALL return the matching employee

#### Scenario: Employee not found by id
- **WHEN** `findById()` is called with an id that has never been saved
- **THEN** it SHALL return an empty result, not throw an exception

### Requirement: Existence check by id
The repository SHALL support checking whether an employee with a given id exists without fetching the full record.

#### Scenario: Existing employee reports true
- **WHEN** `existsById()` is called with an id that was previously saved
- **THEN** it SHALL return `true`

#### Scenario: Non-existent employee reports false
- **WHEN** `existsById()` is called with an id that has never been saved
- **THEN** it SHALL return `false`

### Requirement: Deletion by id
The repository SHALL support deleting an employee by id, and deleting a non-existent id SHALL be a safe no-op.

#### Scenario: Deleting an existing employee removes it
- **WHEN** `deleteById()` is called with an id that was previously saved
- **THEN** subsequent `existsById()` and `findAll()` calls SHALL no longer include that employee

#### Scenario: Deleting a non-existent id does not error
- **WHEN** `deleteById()` is called with an id that was never saved
- **THEN** the call SHALL complete without throwing an exception

### Requirement: Durable storage
Employee data SHALL persist across application restarts when the active `EmployeeRepository` implementation is backed by MySQL.

#### Scenario: Data survives a restart
- **WHEN** an employee is created and the application is subsequently restarted with the same MySQL database
- **THEN** the employee SHALL still be retrievable via `findAll()` or `findById()` after restart

