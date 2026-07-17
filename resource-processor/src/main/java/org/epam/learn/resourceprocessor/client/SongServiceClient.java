package org.epam.learn.resourceprocessor.client;

import org.epam.learn.resourceprocessor.model.SongMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SongServiceClient {

    @Value("${song-service.api.url}")
    private String songServiceUrl;

    private final RestClient restClient = RestClient.create();

    @Retryable(includes = {RestClientException.class},
            maxRetries = 3,
            delay = 1000,
            multiplier = 2
    )
    public void saveSongMetadata(SongMetadata metadata) {
        restClient.post()
                .uri(songServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(metadata)
                .retrieve()
                .toBodilessEntity();
    }
}
