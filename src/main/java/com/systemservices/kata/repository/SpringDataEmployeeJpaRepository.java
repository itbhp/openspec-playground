package com.systemservices.kata.repository;

import com.systemservices.kata.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEmployeeJpaRepository extends JpaRepository<Employee, Long> {
}
