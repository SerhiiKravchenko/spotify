package org.epam.learn.resource_service.component;

import java.net.URI;
import java.time.Duration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.epam.learn.resource_service.client.StorageServiceClient;
import org.epam.learn.resource_service.model.StorageDto;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ComponentTestConfig.ContainersInitializer.class)
@Import(ComponentTestConfig.StorageStubConfig.class)
public class ComponentTestConfig {

    public static final String STAGING_BUCKET = "staging-bucket";
    public static final String PERMANENT_BUCKET = "permanent-bucket";

    public static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17");

    @SuppressWarnings("resource")
    public static final GenericContainer<?> LOCALSTACK =
            new GenericContainer<>(DockerImageName.parse("localstack/localstack:3.8.1"))
                    .withExposedPorts(4566)
                    .withEnv("SERVICES", "s3")
                    .waitingFor(Wait.forLogMessage(".*Ready\\..*", 1)
                            .withStartupTimeout(Duration.ofMinutes(2)));

    public static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    static {
        POSTGRES.start();
        LOCALSTACK.start();
        RABBITMQ.start();
    }

    public static URI localstackEndpoint() {
        return URI.create("http://localhost:" + LOCALSTACK.getMappedPort(4566));
    }

    static class ContainersInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "aws.s3.endpoint=" + localstackEndpoint(),
                    "spring.rabbitmq.host=" + RABBITMQ.getHost(),
                    "spring.rabbitmq.port=" + RABBITMQ.getAmqpPort(),
                    "eureka.client.enabled=false",
                    "spring.main.allow-bean-definition-overriding=true"
            ).applyTo(context);
        }
    }

    @TestConfiguration
    public static class StorageStubConfig {

        @Bean
        @Primary
        public StorageServiceClient stubStorageServiceClient(S3Client s3Client) {
            createBucketIfMissing(s3Client, STAGING_BUCKET);
            createBucketIfMissing(s3Client, PERMANENT_BUCKET);

            StorageServiceClient stub = mock(StorageServiceClient.class);
            when(stub.getStaging())
                    .thenReturn(new StorageDto(1L, "STAGING", STAGING_BUCKET, "/files"));
            when(stub.getPermanent())
                    .thenReturn(new StorageDto(2L, "PERMANENT", PERMANENT_BUCKET, "/files"));
            return stub;
        }

        private void createBucketIfMissing(S3Client s3Client, String bucket) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            } catch (S3Exception e) {
                /* just ignored */
            }
        }
    }
}
