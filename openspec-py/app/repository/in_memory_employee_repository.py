from typing import Optional

from app.model.employee import Employee
from app.repository.employee_repository import EmployeeRepository


class InMemoryEmployeeRepository(EmployeeRepository):
    def __init__(self) -> None:
        self._store: dict[int, Employee] = {}
        self._sequence = 1

    def find_all(self) -> list[Employee]:
        return list(self._store.values())

    def find_by_id(self, id: int) -> Optional[Employee]:
        return self._store.get(id)

    def save(self, employee: Employee) -> Employee:
        if employee.id is None:
            employee.id = self._sequence
            self._sequence += 1
        self._store[employee.id] = employee
        return employee

    def delete_by_id(self, id: int) -> None:
        self._store.pop(id, None)

    def exists_by_id(self, id: int) -> bool:
        return id in self._store

