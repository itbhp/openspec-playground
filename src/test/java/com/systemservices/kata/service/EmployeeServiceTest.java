package com.systemservices.kata.service;

import com.systemservices.kata.model.Employee;
import com.systemservices.kata.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @Mock
  private EmployeeRepository repository;

  @InjectMocks
  private EmployeeService service;

  private static Employee employee(Long id) {
    Employee e = new Employee();
    e.setId(id);
    e.setFirstName("Alice");
    return e;
  }

  @Test
  void findAll_returnsAllEmployeesFromRepository() {
    List<Employee> employees = List.of(employee(1L), employee(2L));
    when(repository.findAll()).thenReturn(employees);

    assertThat(service.findAll()).isEqualTo(employees);
  }

  @Test
  void findById_returnsEmployee_whenRepositoryHasIt() {
    Employee e = employee(1L);
    when(repository.findById(1L)).thenReturn(Optional.of(e));

    assertThat(service.findById(1L)).contains(e);
  }

  @Test
  void findById_returnsEmpty_whenRepositoryDoesNotHaveIt() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThat(service.findById(1L)).isEmpty();
  }

  @Test
  void create_savesEmployeeThroughRepository() {
    Employee toCreate = employee(null);
    Employee saved = employee(1L);
    when(repository.save(toCreate)).thenReturn(saved);

    assertThat(service.create(toCreate)).isEqualTo(saved);
  }

  @Test
  void update_setsGivenIdOnEmployeeBeforeSaving() {
    Employee toUpdate = employee(null);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.update(42L, toUpdate);

    ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo(42L);
  }

  @Test
  void update_returnsEmployeeReturnedByRepository() {
    Employee toUpdate = employee(null);
    Employee saved = employee(42L);
    when(repository.save(toUpdate)).thenReturn(saved);

    assertThat(service.update(42L, toUpdate)).isEqualTo(saved);
  }

  @Test
  void delete_removesEmployeeThroughRepository() {
    service.delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  void exists_returnsTrue_whenRepositoryHasEmployee() {
    when(repository.existsById(1L)).thenReturn(true);

    assertThat(service.exists(1L)).isTrue();
  }

  @Test
  void exists_returnsFalse_whenRepositoryDoesNotHaveEmployee() {
    when(repository.existsById(1L)).thenReturn(false);

    assertThat(service.exists(1L)).isFalse();
  }
}
