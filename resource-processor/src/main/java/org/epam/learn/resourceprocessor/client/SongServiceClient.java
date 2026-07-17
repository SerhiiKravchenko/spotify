package org.epam.learn.resourceprocessor.client;

import org.epam.learn.resourceprocessor.model.SongMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SongServiceClient {

    @Value("${song-service.api.url}")
    private String songServiceUrl;

    private final RestClient restClient = RestClient.create();

    public void saveSongMetadata(SongMetadata metadata) {
        restClient.post()
                .uri(songServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(metadata)
                .retrieve()
                .toBodilessEntity();
    }
}
