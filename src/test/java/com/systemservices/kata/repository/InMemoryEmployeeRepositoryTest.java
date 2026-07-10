package com.systemservices.kata.repository;

class InMemoryEmployeeRepositoryTest extends EmployeeRepositoryContractTest {

  @Override
  protected EmployeeRepository createRepository() {
    return new InMemoryEmployeeRepository();
  }
}
