from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Response, status
from pydantic import BaseModel

from app.dependencies import get_employee_service
from app.model.employee import Employee
from app.service.employee_service import EmployeeService

router = APIRouter(prefix="/employees", tags=["employees"])


class EmployeeIn(BaseModel):
    firstName: Optional[str] = None
    lastName: Optional[str] = None
    email: Optional[str] = None
    department: Optional[str] = None


class EmployeeOut(BaseModel):
    id: int
    firstName: Optional[str] = None
    lastName: Optional[str] = None
    email: Optional[str] = None
    department: Optional[str] = None


def _to_out(e: Employee) -> EmployeeOut:
    return EmployeeOut(
        id=e.id,
        firstName=e.first_name,
        lastName=e.last_name,
        email=e.email,
        department=e.department,
    )


def _to_domain(dto: EmployeeIn) -> Employee:
    return Employee(
        first_name=dto.firstName,
        last_name=dto.lastName,
        email=dto.email,
        department=dto.department,
    )


@router.get("", response_model=list[EmployeeOut])
def list_employees(service: EmployeeService = Depends(get_employee_service)):
    return [_to_out(e) for e in service.find_all()]


@router.post("", response_model=EmployeeOut, status_code=status.HTTP_201_CREATED)
def create_employee(
    dto: EmployeeIn, service: EmployeeService = Depends(get_employee_service)
):
    return _to_out(service.create(_to_domain(dto)))


@router.get("/{id}", response_model=EmployeeOut)
def get_employee(id: int, service: EmployeeService = Depends(get_employee_service)):
    employee = service.find_by_id(id)
    if employee is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    return _to_out(employee)


@router.put("/{id}", response_model=EmployeeOut)
def update_employee(
    id: int, dto: EmployeeIn, service: EmployeeService = Depends(get_employee_service)
):
    if not service.exists(id):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    return _to_out(service.update(id, _to_domain(dto)))


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_employee(id: int, service: EmployeeService = Depends(get_employee_service)):
    if not service.exists(id):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND)
    service.delete(id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)

