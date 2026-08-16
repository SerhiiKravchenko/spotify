package org.epam.learn.resource_service.model;

public record StorageDto(Long id, String storageType, String bucket, String path) {
}
