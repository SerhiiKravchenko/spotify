package org.epam.learn.resource_service.contract;

import java.util.UUID;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;

import org.epam.learn.resource_service.component.ComponentTestConfig;
import org.epam.learn.resource_service.configuration.S3Properties;
import org.epam.learn.resource_service.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Provider("resource-service")
@PactFolder("src/test/resources/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ResourceServicePactProviderTest.ContainersInitializer.class)
public class ResourceServicePactProviderTest {

    private static final UUID RESOURCE_KEY = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final byte[] SAMPLE_MP3 = {(byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x00};

    static {
        if (!ComponentTestConfig.POSTGRES.isRunning()) ComponentTestConfig.POSTGRES.start();
        if (!ComponentTestConfig.LOCALSTACK.isRunning()) ComponentTestConfig.LOCALSTACK.start();
        if (!ComponentTestConfig.RABBITMQ.isRunning()) ComponentTestConfig.RABBITMQ.start();
    }

    @LocalServerPort
    int port;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Properties s3Properties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void configureTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("a resource with ID 7 exists")
    void setupResourceWithId7() {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(s3Properties.getBucketName())
                        .key(RESOURCE_KEY.toString())
                        .contentType("audio/mpeg")
                        .build(),
                RequestBody.fromBytes(SAMPLE_MP3)
        );
        jdbcTemplate.update(
                "INSERT INTO urls (id, key, url) VALUES (7, ?::uuid, ?) "
                        + "ON CONFLICT (id) DO UPDATE SET key = EXCLUDED.key, url = EXCLUDED.url",
                RESOURCE_KEY.toString(),
                ComponentTestConfig.localstackEndpoint()
                        + "/" + s3Properties.getBucketName() + "/" + RESOURCE_KEY
        );
        jdbcTemplate.execute("ALTER SEQUENCE IF EXISTS urls_id_seq RESTART WITH 100");
    }

    @State("no resource exists with ID 99")
    void ensureNoResourceWithId99() {
        resourceRepository.deleteAll();
    }

    static class ContainersInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + ComponentTestConfig.POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + ComponentTestConfig.POSTGRES.getUsername(),
                    "spring.datasource.password=" + ComponentTestConfig.POSTGRES.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "aws.s3.endpoint=" + ComponentTestConfig.localstackEndpoint(),
                    "spring.rabbitmq.host=" + ComponentTestConfig.RABBITMQ.getHost(),
                    "spring.rabbitmq.port=" + ComponentTestConfig.RABBITMQ.getAmqpPort(),
                    "eureka.client.enabled=false",
                    "spring.main.allow-bean-definition-overriding=true"
            ).applyTo(context);
        }
    }
}
