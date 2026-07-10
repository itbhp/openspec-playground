package com.systemservices.kata.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

// Re-enables JPA/DataSource autoconfiguration, which is off by default in
// application.properties now that DynamoDB is the active store (Act 3).
@SpringBootTest(properties = "spring.autoconfigure.exclude=")
@Testcontainers
class JpaEmployeeRepositoryTest extends EmployeeRepositoryContractTest {

  @Container
  static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

  // DynamoDbEmployeeRepository (the now-active EmployeeRepository bean) creates its
  // table eagerly in its constructor against this client — mock it so the JPA-focused
  // context doesn't need a real DynamoDB endpoint reachable.
  @MockitoBean private DynamoDbClient dynamoDbClient;

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @Autowired
  private SpringDataEmployeeJpaRepository springDataEmployeeJpaRepository;

  @Override
  protected EmployeeRepository createRepository() {
    // JpaEmployeeRepository is no longer a Spring bean (dormant since Act 3),
    // so it's constructed directly around the still-managed Spring Data repository.
    return new JpaEmployeeRepository(springDataEmployeeJpaRepository);
  }
}
