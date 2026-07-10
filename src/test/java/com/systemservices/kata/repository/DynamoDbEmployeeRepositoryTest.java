package com.systemservices.kata.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// JPA/DataSource autoconfiguration is off by default (application.properties) since
// DynamoDB is the active store — no MySQL instance is needed or available here.
@SpringBootTest
@Testcontainers
class DynamoDbEmployeeRepositoryTest extends EmployeeRepositoryContractTest {

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

  @Autowired
  private DynamoDbEmployeeRepository dynamoDbEmployeeRepository;

  @Override
  protected EmployeeRepository createRepository() {
    return dynamoDbEmployeeRepository;
  }
}
