package com.systemservices.kata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.systemservices.kata.model.Employee;
import com.systemservices.kata.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private EmployeeService service;

  private static Employee employee(Long id) {
    Employee e = new Employee();
    e.setId(id);
    e.setFirstName("Alice");
    e.setLastName("Doe");
    e.setEmail("alice@example.com");
    e.setDepartment("Engineering");
    return e;
  }

  @Test
  void list_returnsAllEmployeesAsJson() throws Exception {
    when(service.findAll()).thenReturn(List.of(employee(1L), employee(2L)));

    mockMvc.perform(get("/employees"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].firstName", is("Alice")));
  }

  @Test
  void create_returns201WithCreatedEmployee() throws Exception {
    Employee saved = employee(1L);
    when(service.create(any(Employee.class))).thenReturn(saved);

    mockMvc.perform(post("/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(employee(null))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.firstName", is("Alice")));
  }

  @Test
  void get_returns200WithEmployee_whenFound() throws Exception {
    when(service.findById(1L)).thenReturn(Optional.of(employee(1L)));

    mockMvc.perform(get("/employees/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  void get_returns404_whenNotFound() throws Exception {
    when(service.findById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/employees/{id}", 1L))
        .andExpect(status().isNotFound());
  }

  @Test
  void update_returns200WithUpdatedEmployee_whenExists() throws Exception {
    when(service.exists(1L)).thenReturn(true);
    when(service.update(eq(1L), any(Employee.class))).thenReturn(employee(1L));

    mockMvc.perform(put("/employees/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(employee(null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  void update_returns404_whenEmployeeDoesNotExist() throws Exception {
    when(service.exists(1L)).thenReturn(false);

    mockMvc.perform(put("/employees/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(employee(null))))
        .andExpect(status().isNotFound());

    verify(service, never()).update(any(), any());
  }

  @Test
  void delete_returns204_whenEmployeeExists() throws Exception {
    when(service.exists(1L)).thenReturn(true);

    mockMvc.perform(delete("/employees/{id}", 1L))
        .andExpect(status().isNoContent());

    verify(service).delete(1L);
  }

  @Test
  void delete_returns404_whenEmployeeDoesNotExist() throws Exception {
    when(service.exists(1L)).thenReturn(false);

    mockMvc.perform(delete("/employees/{id}", 1L))
        .andExpect(status().isNotFound());

    verify(service, never()).delete(any());
  }
}
