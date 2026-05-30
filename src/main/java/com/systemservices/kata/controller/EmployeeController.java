package com.systemservices.kata.controller;

import com.systemservices.kata.model.Employee;
import com.systemservices.kata.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

  private final EmployeeService svc;

  public EmployeeController(EmployeeService svc) {
    this.svc = svc;
  }

  @GetMapping
  public List<Employee> list() {
    return svc.findAll();
  }

  @PostMapping
  public ResponseEntity<Employee> create(@RequestBody Employee e) {
    return ResponseEntity.status(CREATED).body(svc.create(e));
  }

  @GetMapping("/{id}")
  public Employee get(@PathVariable Long id) {
    return svc.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
  }

  @PutMapping("/{id}")
  public Employee update(@PathVariable Long id, @RequestBody Employee e) {
    if (!svc.exists(id)) {
      throw new ResponseStatusException(NOT_FOUND);
    }
    return svc.update(id, e);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!svc.exists(id)) {
      throw new ResponseStatusException(NOT_FOUND);
    }
    svc.delete(id);
    return ResponseEntity.noContent().build();
  }
}