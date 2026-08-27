package org.epam.learn.resourceprocessor.client;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class ServiceTokenProvider {

    private static final long EXPIRY_SKEW_SECONDS = 30;

    private final RestClient tokenClient = RestClient.create();

    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public ServiceTokenProvider(
            @Value("${service.auth.token-uri}") String tokenUri,
            @Value("${service.auth.client-id}") String clientId,
            @Value("${service.auth.client-secret}") String clientSecret) {
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt)) {
            return cachedToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        Map<String, Object> response = tokenClient.post()
                .uri(tokenUri)
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Token endpoint returned no access_token");
        }

        String token = (String) response.get("access_token");
        Number expiresIn = (Number) response.getOrDefault("expires_in", 300);
        this.cachedToken = token;
        this.expiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn.longValue() - EXPIRY_SKEW_SECONDS));
        return token;
    }
}
