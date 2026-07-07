from functools import lru_cache

from app.repository.employee_repository import EmployeeRepository
from app.repository.in_memory_employee_repository import InMemoryEmployeeRepository
from app.service.employee_service import EmployeeService


# Active persistence: in-memory. Swap this binding in Act 2 (MySQL/SQLAlchemy)
# or Act 3 (DynamoDB). Controller and service never change.
@lru_cache
def get_employee_repository() -> EmployeeRepository:
    return InMemoryEmployeeRepository()


def get_employee_service() -> EmployeeService:
    return EmployeeService(get_employee_repository())

