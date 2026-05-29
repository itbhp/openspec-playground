package com.systemservices.kata.repository;

import com.systemservices.kata.model.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

  private final Map<Long, Employee> store = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(1);

  @Override
  public List<Employee> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public Optional<Employee> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Employee save(Employee e) {
    if (e.getId() == null) {
      e.setId(seq.getAndIncrement());
    }
    store.put(e.getId(), e);
    return e;
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }

  @Override
  public boolean existsById(Long id) {
    return store.containsKey(id);
  }
}