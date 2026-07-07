from typing import Optional

from app.model.employee import Employee
from app.repository.employee_repository import EmployeeRepository


class EmployeeService:
    def __init__(self, repository: EmployeeRepository) -> None:
        self._repository = repository

    def find_all(self) -> list[Employee]:
        return self._repository.find_all()

    def find_by_id(self, id: int) -> Optional[Employee]:
        return self._repository.find_by_id(id)

    def create(self, employee: Employee) -> Employee:
        return self._repository.save(employee)

    def update(self, id: int, employee: Employee) -> Employee:
        employee.id = id
        return self._repository.save(employee)

    def delete(self, id: int) -> None:
        self._repository.delete_by_id(id)

    def exists(self, id: int) -> bool:
        return self._repository.exists_by_id(id)

