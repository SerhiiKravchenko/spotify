package org.epam.learn.resourceprocessor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ResourceServiceClient {

    @Value("${resource-service.api.url}")
    private String resourceServiceUrl;

    private final RestClient restClient = RestClient.create();
    private final RetryTemplate retryTemplate;

    public ResourceServiceClient(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

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
