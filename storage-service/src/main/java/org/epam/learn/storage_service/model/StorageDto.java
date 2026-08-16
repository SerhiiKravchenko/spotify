package org.epam.learn.storage_service.model;

import jakarta.validation.constraints.NotBlank;

public class StorageDto {

    private Long id;

    @NotBlank(message = "Storage type is required")
    private String storageType;

    @NotBlank(message = "Bucket is required")
    private String bucket;

    @NotBlank(message = "Path is required")
    private String path;

    public StorageDto() {
    }

    public StorageDto(Long id, String storageType, String bucket, String path) {
        this.id = id;
        this.storageType = storageType;
        this.bucket = bucket;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
