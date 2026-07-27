package org.epam.learn.resource_service.component;

import java.net.URI;
import java.time.Duration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ComponentTestConfig.ContainersInitializer.class)
public class ComponentTestConfig {

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
}
