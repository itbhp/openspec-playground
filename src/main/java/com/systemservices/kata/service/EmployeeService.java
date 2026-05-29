package com.systemservices.kata.service;

import com.systemservices.kata.model.Employee;
import com.systemservices.kata.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

  private final EmployeeRepository repo;

  public EmployeeService(EmployeeRepository repo) {
    this.repo = repo;
  }

  public List<Employee> findAll() {
    return repo.findAll();
  }

  public Optional<Employee> findById(Long id) {
    return repo.findById(id);
  }

  public Employee create(Employee e) {
    return repo.save(e);
  }

  public Employee update(Long id, Employee e) {
    e.setId(id);
    return repo.save(e);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }

  public boolean exists(Long id) {
    return repo.existsById(id);
  }
}