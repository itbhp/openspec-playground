import { Employee } from '../model/employee';

export interface EmployeeRepository {
  findAll(): Promise<Employee[]>;
  findById(id: number): Promise<Employee | null>;
  save(employee: Employee): Promise<Employee>;
  deleteById(id: number): Promise<void>;
  existsById(id: number): Promise<boolean>;
}

// DI token — TS interfaces don't exist at runtime, so we bind by token.
export const EMPLOYEE_REPOSITORY = Symbol('EmployeeRepository');

