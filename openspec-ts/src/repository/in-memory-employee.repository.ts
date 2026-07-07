import { Injectable } from '@nestjs/common';
import { Employee } from '../model/employee';
import { EmployeeRepository } from './employee-repository.interface';

@Injectable()
export class InMemoryEmployeeRepository implements EmployeeRepository {
  private readonly store = new Map<number, Employee>();
  private sequence = 1;

  async findAll(): Promise<Employee[]> {
    return Array.from(this.store.values());
  }

  async findById(id: number): Promise<Employee | null> {
    return this.store.get(id) ?? null;
  }

  async save(employee: Employee): Promise<Employee> {
    if (employee.id === null) {
      employee.id = this.sequence++;
    }
    this.store.set(employee.id, employee);
    return employee;
  }

  async deleteById(id: number): Promise<void> {
    this.store.delete(id);
  }

  async existsById(id: number): Promise<boolean> {
    return this.store.has(id);
  }
}

