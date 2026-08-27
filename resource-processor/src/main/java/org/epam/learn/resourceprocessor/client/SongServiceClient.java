package org.epam.learn.resourceprocessor.client;

import org.epam.learn.resourceprocessor.model.SongMetadata;
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
public class SongServiceClient {

    @Value("${song-service.api.url}")
    private String songServiceUrl;

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    public SongServiceClient(RetryTemplate retryTemplate, ServiceTokenProvider tokenProvider) {
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
    public void saveSongMetadata(SongMetadata metadata) {
        retryTemplate.invoke(() ->
            restClient.post()
                    .uri(songServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(metadata)
                    .retrieve()
                    .toBodilessEntity()
        );
    }
}
