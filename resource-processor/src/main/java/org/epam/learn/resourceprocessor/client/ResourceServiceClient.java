package org.epam.learn.resourceprocessor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RefreshScope
public class ResourceServiceClient {

    @Value("${resource-service.api.url}")
    private String resourceServiceUrl;

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    public ResourceServiceClient(RetryTemplate retryTemplate, ServiceTokenProvider tokenProvider) {
        this.retryTemplate = retryTemplate;
        this.restClient = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenProvider.getToken());
                    return execution.execute(request, body);
                })
                .build();
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
