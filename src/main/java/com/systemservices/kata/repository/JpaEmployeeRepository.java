package com.systemservices.kata.repository;

import com.systemservices.kata.model.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;

// Dormant: superseded by DynamoDbEmployeeRepository (Act 3). Kept for the repository
// contract test suite (JpaEmployeeRepositoryTest) and as a reference for MySQL usage.
public class JpaEmployeeRepository implements EmployeeRepository {

  private final SpringDataEmployeeJpaRepository jpaRepository;

  public JpaEmployeeRepository(SpringDataEmployeeJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<Employee> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public Optional<Employee> findById(Long id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Employee save(Employee employee) {
    return jpaRepository.save(employee);
  }

  @Override
  public void deleteById(Long id) {
    try {
      jpaRepository.deleteById(id);
    } catch (EmptyResultDataAccessException ignored) {
      // deleting a non-existent id is a no-op, matching InMemoryEmployeeRepository
    }
  }

  @Override
  public boolean existsById(Long id) {
    return jpaRepository.existsById(id);
  }
}
