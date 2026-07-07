<?php

declare(strict_types=1);

namespace App\Repository;

use App\Model\Employee;

interface EmployeeRepositoryInterface
{
    /**
     * @return Employee[]
     */
    public function findAll(): array;

    public function findById(int $id): ?Employee;

    public function save(Employee $employee): Employee;

    public function deleteById(int $id): void;

    public function existsById(int $id): bool;
}

