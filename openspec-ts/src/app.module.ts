import { Module } from '@nestjs/common';
import { EmployeeController } from './controller/employee.controller';
import { EmployeeService } from './service/employee.service';
import { EMPLOYEE_REPOSITORY } from './repository/employee-repository.interface';
import { InMemoryEmployeeRepository } from './repository/in-memory-employee.repository';

@Module({
  controllers: [EmployeeController],
  providers: [
    EmployeeService,
    // Active persistence: in-memory. Swap this binding in Act 2 (MySQL/MikroORM)
    // or Act 3 (DynamoDB). Controller and service never change.
    { provide: EMPLOYEE_REPOSITORY, useClass: InMemoryEmployeeRepository },
  ],
})
export class AppModule {}

