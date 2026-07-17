package org.epam.learn.resource_service.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Configuration
public class S3BucketInitializer {

    private final S3Properties props;

    public S3BucketInitializer(S3Properties props) {
        this.props = props;
    }

    @Bean
    public ApplicationRunner createBucket(S3Client s3Client) {
        return args -> {
            boolean exists = s3Client.listBuckets()
                    .buckets()
                    .stream()
                    .anyMatch(b -> b.name().equals(props.getBucketName()));

            if (!exists) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(props.getBucketName())
                        .build());
            }
        };
    }
}
