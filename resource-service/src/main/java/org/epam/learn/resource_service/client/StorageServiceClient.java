package org.epam.learn.resource_service.client;

import java.util.List;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.model.StorageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@RefreshScope
public class StorageServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StorageServiceClient.class);

    private static final String CIRCUIT_BREAKER = "storageService";

    private static final String STAGING_TYPE = "STAGING";
    private static final String PERMANENT_TYPE = "PERMANENT";
    private static final String STORAGE_TYPE_NOT_FOUND_MESSAGE = "Storage type=%s not found";

    private static final String STAGING_STUB_BUCKET = "staging-bucket";
    private static final String PERMANENT_STUB_BUCKET = "permanent-bucket";
    private static final String STUB_PATH = "/files";

    @Value("${storage-service.api.url}")
    private String storageServiceUrl;

    private final RestClient restClient = RestClient.create();
    private final RetryTemplate retryTemplate;

    public StorageServiceClient(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "getStagingFallback")
    public StorageDto getStaging() {
        return getByType(STAGING_TYPE);
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "getPermanentFallback")
    public StorageDto getPermanent() {
        return getByType(PERMANENT_TYPE);
    }

    private StorageDto getByType(String storageType) {
        List<StorageDto> storages = retryTemplate.invoke(() -> restClient.get()
                .uri(storageServiceUrl)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));

        return isEmpty(storages) ? null : storages.stream()
                .filter(storage -> storageType.equals(storage.storageType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(STORAGE_TYPE_NOT_FOUND_MESSAGE, storageType)));
    }

    @SuppressWarnings("unused")
    private StorageDto getStagingFallback(Throwable t) {
        log.warn("Storage service unavailable, returning STAGING stub", t);
        return new StorageDto(null, STAGING_TYPE, STAGING_STUB_BUCKET, STUB_PATH);
    }

    @SuppressWarnings("unused")
    private StorageDto getPermanentFallback(Throwable t) {
        log.warn("Storage service unavailable, returning PERMANENT stub", t);
        return new StorageDto(null, PERMANENT_TYPE, PERMANENT_STUB_BUCKET, STUB_PATH);
    }
}
