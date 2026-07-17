package org.epam.learn.resource_service.client;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SongServiceClient {

    private static final String ID_PATH_PARAMETER = "?id=";
    private static final String COMMA_DELIMITER = ",";

    @Value("${song-service.api.url}")
    private String songServiceUrl;

    private final RestClient restClient = RestClient.create();

    @Retryable(
            includes = {RestClientException.class},
            maxRetries = 3,
            delay = 1000,
            multiplier = 2
    )
    public void deleteSongs(List<Long> ids) {
        restClient.delete()
                .uri(URI.create(songServiceUrl + ID_PATH_PARAMETER + String.join(COMMA_DELIMITER,
                        ids.stream().map(String::valueOf).toList())))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }
}
