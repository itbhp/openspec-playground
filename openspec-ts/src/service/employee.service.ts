import { Inject, Injectable } from '@nestjs/common';
import { Employee } from '../model/employee';
import {
  EMPLOYEE_REPOSITORY,
  EmployeeRepository,
} from '../repository/employee-repository.interface';

@Injectable()
export class EmployeeService {
  constructor(
    @Inject(EMPLOYEE_REPOSITORY)
    private readonly repository: EmployeeRepository,
  ) {}

  findAll(): Promise<Employee[]> {
    return this.repository.findAll();
  }

  findById(id: number): Promise<Employee | null> {
    return this.repository.findById(id);
  }

  create(employee: Employee): Promise<Employee> {
    return this.repository.save(employee);
  }

  update(id: number, employee: Employee): Promise<Employee> {
    employee.id = id;
    return this.repository.save(employee);
  }

  delete(id: number): Promise<void> {
    return this.repository.deleteById(id);
  }

  exists(id: number): Promise<boolean> {
    return this.repository.existsById(id);
  }
}

