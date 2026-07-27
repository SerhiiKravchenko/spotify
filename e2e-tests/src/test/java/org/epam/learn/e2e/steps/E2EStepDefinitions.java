package org.epam.learn.e2e.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import org.awaitility.Awaitility;
import org.epam.learn.e2e.SharedStack;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class E2EStepDefinitions {

    private static final AtomicBoolean PLATFORM_READY = new AtomicBoolean(false);

    private Response lastResponse;

    @Given("the full platform is running and healthy")
    public void platformIsRunningAndHealthy() {
        if (PLATFORM_READY.compareAndSet(false, true)) {
            Awaitility.await("gateway routing to resource-service")
                    .atMost(2, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            given().baseUri(SharedStack.gatewayBaseUri())
                                    .get("/resources/99999")
                                    .then().statusCode(404));
        }
    }

    @When("I upload a valid MP3 file via POST \\/resources")
    public void uploadMp3() {
        byte[] mp3Bytes = new byte[]{(byte) 0xFF, (byte) 0xFB, 0x10, 0x00, 0x00, 0x00};
        lastResponse = given()
                .baseUri(SharedStack.gatewayBaseUri())
                .contentType("audio/mpeg")
                .body(mp3Bytes)
                .post("/resources");
    }

    @When("I request GET \\/resources\\/{long}")
    public void requestGetResourceById(long id) {
        lastResponse = given()
                .baseUri(SharedStack.gatewayBaseUri())
                .get("/resources/{id}", id);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        assertThat(lastResponse.statusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    @And("the response contains a resource ID")
    public void theResponseContainsAResourceId() {
        int id = lastResponse.jsonPath().getInt("id");
        assertThat(id).as("resource ID in response body").isPositive();
    }

    @And("the response body contains an error message")
    public void theResponseBodyContainsAnErrorMessage() {
        assertThat(lastResponse.body().asString())
                .as("error response body")
                .isNotBlank();
    }
}
