package com.systemservices.kata.repository;

import com.systemservices.kata.model.Employee;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Repository
public class DynamoDbEmployeeRepository implements EmployeeRepository {

  static final String TABLE_NAME = "employees";
  private static final String KEY_ATTRIBUTE = "id";
  private static final String COUNTER_ID = "EMPLOYEE_ID_SEQ";
  private static final String COUNTER_VALUE_ATTRIBUTE = "value";

  private final DynamoDbClient client;

  public DynamoDbEmployeeRepository(DynamoDbClient client) {
    this.client = client;
    ensureTableExists();
  }

  private void ensureTableExists() {
    try {
      client.createTable(
          CreateTableRequest.builder()
              .tableName(TABLE_NAME)
              .billingMode(BillingMode.PAY_PER_REQUEST)
              .attributeDefinitions(
                  AttributeDefinition.builder()
                      .attributeName(KEY_ATTRIBUTE)
                      .attributeType(ScalarAttributeType.S)
                      .build())
              .keySchema(
                  KeySchemaElement.builder()
                      .attributeName(KEY_ATTRIBUTE)
                      .keyType(KeyType.HASH)
                      .build())
              .build());
    } catch (ResourceInUseException alreadyExists) {
      // table already provisioned — nothing to do
    }
  }

  private long nextId() {
    UpdateItemResponse response =
        client.updateItem(
            UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of(KEY_ATTRIBUTE, AttributeValue.fromS(COUNTER_ID)))
                .updateExpression("ADD #v :incr")
                .expressionAttributeNames(Map.of("#v", COUNTER_VALUE_ATTRIBUTE))
                .expressionAttributeValues(Map.of(":incr", AttributeValue.fromN("1")))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build());
    return Long.parseLong(response.attributes().get(COUNTER_VALUE_ATTRIBUTE).n());
  }

  @Override
  public List<Employee> findAll() {
    ScanResponse response = client.scan(ScanRequest.builder().tableName(TABLE_NAME).build());
    List<Employee> employees = new ArrayList<>();
    for (Map<String, AttributeValue> item : response.items()) {
      if (COUNTER_ID.equals(item.get(KEY_ATTRIBUTE).s())) {
        continue;
      }
      employees.add(fromItem(item));
    }
    return employees;
  }

  @Override
  public Optional<Employee> findById(Long id) {
    GetItemResponse response =
        client.getItem(GetItemRequest.builder().tableName(TABLE_NAME).key(key(id)).build());
    if (!response.hasItem()) {
      return Optional.empty();
    }
    return Optional.of(fromItem(response.item()));
  }

  @Override
  public Employee save(Employee employee) {
    if (employee.getId() == null) {
      employee.setId(nextId());
    }
    client.putItem(
        PutItemRequest.builder().tableName(TABLE_NAME).item(toItem(employee)).build());
    return employee;
  }

  @Override
  public void deleteById(Long id) {
    client.deleteItem(DeleteItemRequest.builder().tableName(TABLE_NAME).key(key(id)).build());
  }

  @Override
  public boolean existsById(Long id) {
    GetItemResponse response =
        client.getItem(
            GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key(id))
                .projectionExpression(KEY_ATTRIBUTE)
                .build());
    return response.hasItem();
  }

  private static Map<String, AttributeValue> key(Long id) {
    return Map.of(KEY_ATTRIBUTE, AttributeValue.fromS(String.valueOf(id)));
  }

  private static Map<String, AttributeValue> toItem(Employee e) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put(KEY_ATTRIBUTE, AttributeValue.fromS(String.valueOf(e.getId())));
    putIfNotNull(item, "firstName", e.getFirstName());
    putIfNotNull(item, "lastName", e.getLastName());
    putIfNotNull(item, "email", e.getEmail());
    putIfNotNull(item, "department", e.getDepartment());
    return item;
  }

  private static void putIfNotNull(Map<String, AttributeValue> item, String attribute, String value) {
    if (value != null) {
      item.put(attribute, AttributeValue.fromS(value));
    }
  }

  private static Employee fromItem(Map<String, AttributeValue> item) {
    Employee e = new Employee();
    e.setId(Long.valueOf(item.get(KEY_ATTRIBUTE).s()));
    e.setFirstName(stringOrNull(item.get("firstName")));
    e.setLastName(stringOrNull(item.get("lastName")));
    e.setEmail(stringOrNull(item.get("email")));
    e.setDepartment(stringOrNull(item.get("department")));
    return e;
  }

  private static String stringOrNull(AttributeValue value) {
    return value == null ? null : value.s();
  }
}
