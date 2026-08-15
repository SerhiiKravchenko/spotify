package org.epam.learn.storage_service.configuration;

import org.epam.learn.storage_service.model.Storage;
import org.epam.learn.storage_service.repository.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Configuration
@EnableConfigurationProperties(StorageSeedProperties.class)
public class StorageInitializer {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final StorageSeedProperties seed;

    public StorageInitializer(StorageSeedProperties seed) {
        this.seed = seed;
    }

    @Bean
    public ApplicationRunner initStorages(StorageRepository storageRepository, S3Client s3Client) {
        return args -> {
            seedIfAbsent(storageRepository, seed.getStagingType(), seed.getStagingBucket(), seed.getStagingPath());
            seedIfAbsent(storageRepository, seed.getPermanentType(), seed.getPermanentBucket(), seed.getPermanentPath());

            storageRepository.findAll().forEach(storage -> createBucketIfAbsent(s3Client, storage.getBucket()));
        };
    }

    private void seedIfAbsent(StorageRepository storageRepository, String type, String bucket, String path) {
        if (!storageRepository.existsByStorageType(type)) {
            storageRepository.save(new Storage(type, bucket, path));
            log.info("Seeded storage type={} bucket={} path={}", type, bucket, path);
        }
    }

    private void createBucketIfAbsent(S3Client s3Client, String bucket) {
        boolean exists = s3Client.listBuckets()
                .buckets()
                .stream()
                .anyMatch(b -> b.name().equals(bucket));

        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created bucket {}", bucket);
        }
    }
}
