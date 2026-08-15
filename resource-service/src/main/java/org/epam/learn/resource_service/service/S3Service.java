package org.epam.learn.resource_service.service;

import org.epam.learn.resource_service.model.Mp3FileUrl;

public interface S3Service {

    Mp3FileUrl uploadFile(byte[] file, String bucket);

    byte[] downloadFile(String key, String bucket);

    void deleteFile(String key, String bucket);

    void moveFile(String key, String sourceBucket, String destinationBucket);
}
