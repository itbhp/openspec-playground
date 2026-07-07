from abc import ABC, abstractmethod
from typing import Optional

from app.model.employee import Employee


class EmployeeRepository(ABC):
    @abstractmethod
    def find_all(self) -> list[Employee]: ...

    @abstractmethod
    def find_by_id(self, id: int) -> Optional[Employee]: ...

    @abstractmethod
    def save(self, employee: Employee) -> Employee: ...

    @abstractmethod
    def delete_by_id(self, id: int) -> None: ...

    @abstractmethod
    def exists_by_id(self, id: int) -> bool: ...

