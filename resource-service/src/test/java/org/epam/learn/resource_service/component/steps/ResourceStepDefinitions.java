package org.epam.learn.resource_service.component.steps;

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.net.URI;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceStepDefinitions {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResourceStepDefinitions() {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue}")
    private String resourceQueue;

    private ResponseEntity<String> lastResponse;
    private long uploadedResourceId;

    @Before
    public void drainQueue() {
        // discard any stale messages left by previous scenarios
        while (rabbitTemplate.receive(resourceQueue, 100) != null) { /* drain */ }
    }

    @When("I upload a valid MP3 file")
    public void iUploadAValidMp3File() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        // minimal MP3 sync-word header; the service stores bytes as-is without content validation
        byte[] mp3Bytes = new byte[]{(byte) 0xFF, (byte) 0xFB, 0x10, 0x00, 0x00, 0x00};
        lastResponse = restTemplate.postForEntity(
                serverUri("/resources"), new HttpEntity<>(mp3Bytes, headers), String.class);
    }

    @When("^I request GET /resources/(\\d+)$")
    public void iRequestGetResourcesById(long id) {
        lastResponse = restTemplate.getForEntity(serverUri("/resources/" + id), String.class);
    }

    private URI serverUri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("the response contains a positive resource ID")
    public void theResponseContainsAPositiveResourceId() {
        String body = lastResponse.getBody();
        assertThat(body).as("response body").contains("\"id\":");
        int valueStart = body.indexOf("\"id\":") + 5;
        int valueEnd = body.indexOf("}", valueStart);
        uploadedResourceId = Long.parseLong(body.substring(valueStart, valueEnd).trim());
        assertThat(uploadedResourceId).isPositive();
    }

    @And("a message is published to {string} containing that resource ID")
    public void aMessageIsPublishedToQueueContainingResourceId(String queue) {
        Message message = rabbitTemplate.receive(queue, 5_000);
        assertThat(message)
                .as("expected a message in queue '%s' but none arrived within 5 s", queue)
                .isNotNull();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains(String.valueOf(uploadedResourceId));
    }

    @And("the response body contains the error message {string}")
    public void theResponseBodyContainsErrorMessage(String errorSubstring) {
        assertThat(lastResponse.getBody()).contains(errorSubstring);
    }
}
