package org.epam.learn.resourceprocessor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ResourceServiceClient {

    @Value("${resource-service.api.url}")
    private String resourceServiceUrl;

    private final RestClient restClient = RestClient.create();
    private final RetryTemplate retryTemplate;

    public ResourceServiceClient(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @Retryable(includes = {RestClientException.class},
            maxRetries = 3,
            delay = 1000,
            multiplier = 2
    )
    public byte[] getResource(Long resourceId) {
        return retryTemplate.invoke(() ->
            restClient.get()
                    .uri(resourceServiceUrl + "/" + resourceId)
                    .accept(MediaType.parseMediaType("audio/mpeg"))
                    .retrieve()
                    .body(byte[].class)
        );
    }
}
