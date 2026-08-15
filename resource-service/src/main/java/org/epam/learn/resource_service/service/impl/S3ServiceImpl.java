package org.epam.learn.resource_service.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.epam.learn.resource_service.configuration.S3Properties;
import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.epam.learn.resource_service.service.S3Service;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {

    private static final String CONTENT_TYPE_MPEG = "audio/mpeg";
    private final S3Client s3Client;
    private final S3Properties prop;
    private final RetryTemplate retryTemplate;

    public S3ServiceImpl(S3Client s3Client, S3Properties prop, RetryTemplate retryTemplate) {
        this.s3Client = s3Client;
        this.prop = prop;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public Mp3FileUrl uploadFile(byte[] file, String bucket) {
        UUID key = UUID.randomUUID();

        retryTemplate.invoke(() -> s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key.toString())
                        .contentType(CONTENT_TYPE_MPEG)
                        .build(),
                RequestBody.fromBytes(file)
        ));

        String url = buildFileUrl(key.toString(), bucket);

        return new Mp3FileUrl(key, url);
    }

    @Override
    public byte[] downloadFile(String key, String bucket) {
        return retryTemplate.invoke(() -> {
            try {
                return s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build()
                ).readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void deleteFile(String key, String bucket) {
        retryTemplate.invoke(() -> s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        ));
    }

    @Override
    public void moveFile(String key, String sourceBucket, String destinationBucket) {
        retryTemplate.invoke(() -> s3Client.copyObject(
                CopyObjectRequest.builder()
                        .sourceBucket(sourceBucket)
                        .sourceKey(key)
                        .destinationBucket(destinationBucket)
                        .destinationKey(key)
                        .build()
        ));

        retryTemplate.invoke(() -> s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(sourceBucket)
                        .key(key)
                        .build()
        ));
    }

    private String buildFileUrl(String key, String bucket) {
        return prop.getEndpoint() + "/" + bucket + "/" + key;
    }
}
