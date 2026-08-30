package org.epam.learn.uiservice.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StorageClient {

    private final RestClient restClient;

    public StorageClient(@Value("${storage.api.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<StorageView> findAll(String accessToken) {
        List<StorageView> result = restClient.get()
                .uri("/storages")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return result != null ? result : List.of();
    }

    public void create(String accessToken, String storageType, String bucket, String path) {
        restClient.post()
                .uri("/storages")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "storageType", storageType,
                        "bucket", bucket,
                        "path", path))
                .retrieve()
                .toBodilessEntity();
    }

    public void delete(String accessToken, Long id) {
        restClient.method(HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder.path("/storages").queryParam("id", id).build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .toBodilessEntity();
    }
}
