<?php

declare(strict_types=1);

namespace App\Repository;

use App\Model\Employee;

class InMemoryEmployeeRepository implements EmployeeRepositoryInterface
{
    /** @var array<int, Employee> */
    private array $store = [];

    private int $sequence = 1;

    public function findAll(): array
    {
        return array_values($this->store);
    }

    public function findById(int $id): ?Employee
    {
        return $this->store[$id] ?? null;
    }

    public function save(Employee $employee): Employee
    {
        if ($employee->getId() === null) {
            $employee->setId($this->sequence++);
        }
        $this->store[$employee->getId()] = $employee;

        return $employee;
    }

    public function deleteById(int $id): void
    {
        unset($this->store[$id]);
    }

    public function existsById(int $id): bool
    {
        return isset($this->store[$id]);
    }
}

