package com.systemservices.kata;

import com.systemservices.kata.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

// Exercises the app end-to-end through whichever EmployeeRepository is actually wired
// in — currently DynamoDbEmployeeRepository (Act 3), the sole active @Repository bean.
// JPA/DataSource autoconfiguration is off by default (application.properties).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EmployeeApiIntegrationTest {

  @Container
  static final LocalStackContainer LOCALSTACK =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8.1"))
          .withServices(LocalStackContainer.Service.DYNAMODB);

  @DynamicPropertySource
  static void dynamoDbProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "aws.dynamodb.endpoint",
        () -> LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    registry.add("aws.dynamodb.region", LOCALSTACK::getRegion);
  }

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private static Employee newEmployee() {
    Employee e = new Employee();
    e.setFirstName("Alice");
    e.setLastName("Doe");
    e.setEmail("alice@example.com");
    e.setDepartment("Engineering");
    return e;
  }

  @Test
  void fullCrudLifecycle_persistsThroughDynamoDb() {
    ResponseEntity<Employee> created =
        restTemplate.postForEntity(url("/employees"), newEmployee(), Employee.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Long id = created.getBody().getId();
    assertThat(id).isNotNull();

    ResponseEntity<Employee> fetched =
        restTemplate.getForEntity(url("/employees/" + id), Employee.class);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fetched.getBody().getFirstName()).isEqualTo("Alice");

    Employee update = newEmployee();
    update.setFirstName("Alicia");
    restTemplate.put(url("/employees/" + id), update);

    ResponseEntity<Employee> afterUpdate =
        restTemplate.getForEntity(url("/employees/" + id), Employee.class);
    assertThat(afterUpdate.getBody().getFirstName()).isEqualTo("Alicia");

    restTemplate.delete(url("/employees/" + id));

    ResponseEntity<Employee> afterDelete =
        restTemplate.getForEntity(url("/employees/" + id), Employee.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void update_returns404_whenEmployeeDoesNotExist() {
    ResponseEntity<Employee> response = restTemplate.exchange(
        url("/employees/999999"),
        org.springframework.http.HttpMethod.PUT,
        new org.springframework.http.HttpEntity<>(newEmployee()),
        Employee.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
