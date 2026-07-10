package com.systemservices.kata.repository;

import com.systemservices.kata.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract that every {@link EmployeeRepository} implementation must satisfy.
 * Extend this class and implement {@link #createRepository()} to run the
 * contract against a specific implementation.
 */
public abstract class EmployeeRepositoryContractTest {

  protected EmployeeRepository repository;

  /** Provides a fresh, empty repository instance under test for each test case. */
  protected abstract EmployeeRepository createRepository();

  @BeforeEach
  void setUp() {
    repository = createRepository();
  }

  private static Employee newEmployee(String firstName) {
    Employee e = new Employee();
    e.setFirstName(firstName);
    e.setLastName("Doe");
    e.setEmail(firstName.toLowerCase() + "@example.com");
    e.setDepartment("Engineering");
    return e;
  }

  @Test
  void findAll_returnsEmptyList_whenNoEmployeesSaved() {
    assertThat(repository.findAll()).isEmpty();
  }

  @Test
  void findAll_returnsAllSavedEmployees() {
    repository.save(newEmployee("Alice"));
    repository.save(newEmployee("Bob"));

    assertThat(repository.findAll()).hasSize(2);
  }

  @Test
  void save_assignsIdWhenEmployeeHasNoId() {
    Employee saved = repository.save(newEmployee("Alice"));

    assertThat(saved.getId()).isNotNull();
  }

  @Test
  void save_assignsDifferentIdsToSuccessiveNewEmployees() {
    Employee first = repository.save(newEmployee("Alice"));
    Employee second = repository.save(newEmployee("Bob"));

    assertThat(first.getId()).isNotEqualTo(second.getId());
  }

  @Test
  void save_updatesExistingEmployeeInPlace_whenIdAlreadyPresent() {
    Employee saved = repository.save(newEmployee("Alice"));
    Long id = saved.getId();

    Employee updated = newEmployee("Alicia");
    updated.setId(id);
    repository.save(updated);

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findById(id)).get().extracting(Employee::getFirstName).isEqualTo("Alicia");
  }

  @Test
  void findById_returnsEmployee_whenItExists() {
    Employee saved = repository.save(newEmployee("Alice"));

    Optional<Employee> found = repository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getFirstName()).isEqualTo("Alice");
  }

  @Test
  void findById_returnsEmpty_whenIdDoesNotExist() {
    assertThat(repository.findById(999L)).isEmpty();
  }

  @Test
  void existsById_returnsTrue_whenEmployeeExists() {
    Employee saved = repository.save(newEmployee("Alice"));

    assertThat(repository.existsById(saved.getId())).isTrue();
  }

  @Test
  void existsById_returnsFalse_whenEmployeeDoesNotExist() {
    assertThat(repository.existsById(999L)).isFalse();
  }

  @Test
  void deleteById_removesEmployee() {
    Employee saved = repository.save(newEmployee("Alice"));

    repository.deleteById(saved.getId());

    assertThat(repository.existsById(saved.getId())).isFalse();
    assertThat(repository.findAll()).isEmpty();
  }

  @Test
  void deleteById_doesNothing_whenIdDoesNotExist() {
    repository.deleteById(999L);

    assertThat(repository.findAll()).isEmpty();
  }

  @Test
  void findAll_returnsIndependentSnapshot_notLiveView() {
    repository.save(newEmployee("Alice"));

    List<Employee> firstSnapshot = repository.findAll();
    repository.save(newEmployee("Bob"));

    assertThat(firstSnapshot).hasSize(1);
  }
}
