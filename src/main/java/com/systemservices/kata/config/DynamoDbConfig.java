package com.systemservices.kata.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration
public class DynamoDbConfig {

  @Bean
  public DynamoDbClient dynamoDbClient(
      @Value("${aws.dynamodb.endpoint:}") String endpoint,
      @Value("${aws.dynamodb.region}") String region) {
    DynamoDbClientBuilder builder =
        DynamoDbClient.builder()
            .region(Region.of(region))
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")));

    if (!endpoint.isBlank()) {
      builder.endpointOverride(URI.create(endpoint));
    }

    return builder.build();
  }
}
