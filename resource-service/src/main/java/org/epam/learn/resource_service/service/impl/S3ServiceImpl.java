package org.epam.learn.resource_service.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.epam.learn.resource_service.configuration.S3Properties;
import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.epam.learn.resource_service.service.S3Service;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {

    private static final String CONTENT_TYPE_MPEG = "audio/mpeg";
    private final S3Client s3Client;
    private final S3Properties prop;

    public S3ServiceImpl(S3Client s3Client, S3Properties prop) {
        this.s3Client = s3Client;
        this.prop = prop;
    }

    @Override
    public Mp3FileUrl uploadFile(byte[] file) {
        UUID key = UUID.randomUUID();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(prop.getBucketName())
                        .key(key.toString())
                        .contentType(CONTENT_TYPE_MPEG)
                        .build(),
                RequestBody.fromBytes(file)
        );

        String url = buildFileUrl(key.toString());

        return new Mp3FileUrl(key, url);
    }

    @Override
    public byte[] downloadFile(String key) {
        try {
            return s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(prop.getBucketName())
                            .key(key)
                            .build()
            ).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(prop.getBucketName())
                        .key(key)
                        .build()
        );
    }

    private String buildFileUrl(String key) {
        return prop.getEndpoint() + "/" + prop.getBucketName() + "/" + key;
    }
}
