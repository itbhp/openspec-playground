<?php

declare(strict_types=1);

namespace App\Service;

use App\Model\Employee;
use App\Repository\EmployeeRepositoryInterface;

class EmployeeService
{
    public function __construct(
        private readonly EmployeeRepositoryInterface $repository,
    ) {
    }

    /**
     * @return Employee[]
     */
    public function findAll(): array
    {
        return $this->repository->findAll();
    }

    public function findById(int $id): ?Employee
    {
        return $this->repository->findById($id);
    }

    public function create(Employee $employee): Employee
    {
        return $this->repository->save($employee);
    }

    public function update(int $id, Employee $employee): Employee
    {
        $employee->setId($id);

        return $this->repository->save($employee);
    }

    public function delete(int $id): void
    {
        $this->repository->deleteById($id);
    }

    public function exists(int $id): bool
    {
        return $this->repository->existsById($id);
    }
}

