import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  NotFoundException,
  Param,
  ParseIntPipe,
  Post,
  Put,
} from '@nestjs/common';
import { Employee } from '../model/employee';
import { EmployeeService } from '../service/employee.service';

interface EmployeeDto {
  firstName?: string;
  lastName?: string;
  email?: string;
  department?: string;
}

@Controller('employees')
export class EmployeeController {
  constructor(private readonly service: EmployeeService) {}

  @Get()
  list(): Promise<Employee[]> {
    return this.service.findAll();
  }

  @Post()
  @HttpCode(201)
  async create(@Body() dto: EmployeeDto): Promise<Employee> {
    return this.service.create(this.toEmployee(dto));
  }

  @Get(':id')
  async get(@Param('id', ParseIntPipe) id: number): Promise<Employee> {
    const employee = await this.service.findById(id);
    if (employee === null) {
      throw new NotFoundException();
    }
    return employee;
  }

  @Put(':id')
  async update(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: EmployeeDto,
  ): Promise<Employee> {
    if (!(await this.service.exists(id))) {
      throw new NotFoundException();
    }
    return this.service.update(id, this.toEmployee(dto));
  }

  @Delete(':id')
  @HttpCode(204)
  async delete(@Param('id', ParseIntPipe) id: number): Promise<void> {
    if (!(await this.service.exists(id))) {
      throw new NotFoundException();
    }
    await this.service.delete(id);
  }

  private toEmployee(dto: EmployeeDto): Employee {
    const employee = new Employee();
    employee.firstName = dto.firstName ?? null;
    employee.lastName = dto.lastName ?? null;
    employee.email = dto.email ?? null;
    employee.department = dto.department ?? null;
    return employee;
  }
}

