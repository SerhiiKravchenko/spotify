package org.epam.learn.resource_service.service;

import org.epam.learn.resource_service.model.Mp3FileUrl;

public interface S3Service {

    Mp3FileUrl uploadFile(byte[] file);

    byte[] downloadFile(String key);

    void deleteFile(String key);
}
