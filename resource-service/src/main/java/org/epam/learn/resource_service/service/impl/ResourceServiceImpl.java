package org.epam.learn.resource_service.service.impl;

import java.util.List;
import java.util.Map;

import org.epam.learn.resource_service.client.SongServiceClient;
import org.epam.learn.resource_service.client.StorageServiceClient;
import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.messaging.ResourceMessagePublisher;
import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.epam.learn.resource_service.model.StorageDto;
import org.epam.learn.resource_service.repository.ResourceRepository;
import org.epam.learn.resource_service.service.ResourceService;
import org.epam.learn.resource_service.service.S3Service;
import org.epam.learn.resource_service.utility.Utility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ResourceServiceImpl implements ResourceService {

    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource with ID=%d not found";
    private static final String DELETED_ID_KEY = "ids";
    private static final String SAVED_ID_KEY = "id";
    private static final String STATE_STAGING = "STAGING";
    private static final String STATE_PERMANENT = "PERMANENT";

    private final ResourceRepository resourceRepository;
    private final S3Service s3Service;
    private final ResourceMessagePublisher messagePublisher;
    private final SongServiceClient songServiceClient;
    private final StorageServiceClient storageServiceClient;


    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               S3Service s3Service,
                               ResourceMessagePublisher messagePublisher,
                               SongServiceClient songServiceClient,
                               StorageServiceClient storageServiceClient) {
        this.resourceRepository = resourceRepository;
        this.s3Service = s3Service;
        this.messagePublisher = messagePublisher;
        this.songServiceClient = songServiceClient;
        this.storageServiceClient = storageServiceClient;
    }

    @Override
    @Transactional
    public Map<String, Long> upload(byte[] file) {
        StorageDto staging = storageServiceClient.getStaging();

        Mp3FileUrl mp3FileUrl = s3Service.uploadFile(file, staging.bucket());
        mp3FileUrl.setState(STATE_STAGING);
        mp3FileUrl.setBucket(staging.bucket());
        mp3FileUrl.setPath(staging.path());

        Mp3FileUrl savedToDb = resourceRepository.save(mp3FileUrl);
        messagePublisher.publishResourceUploaded(savedToDb.getId());

        return Map.of(SAVED_ID_KEY, savedToDb.getId());
    }

    @Override
    public byte[] download(Long fileId) {
        Utility.isValidId(fileId);

        Mp3FileUrl mp3FileUrl = resourceRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, fileId)));

        return s3Service.downloadFile(mp3FileUrl.getKey().toString(), mp3FileUrl.getBucket());
    }

    @Override
    @Transactional
    public Map<String, List<Long>> delete(List<String> fileIds) {
        Utility.validateStringIdsForDeletion(fileIds);

        List<Long> deleted = fileIds.stream()
                .map(Long::parseLong)
                .filter(resourceRepository::existsById)
                .toList();

        for (Long id : deleted) {
            Mp3FileUrl mp3FileUrl = resourceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, id)));

            resourceRepository.deleteById(id);
            s3Service.deleteFile(mp3FileUrl.getKey().toString(), mp3FileUrl.getBucket());
        }

        if (!deleted.isEmpty()) {
            songServiceClient.deleteSongs(deleted);
        }

        return Map.of(DELETED_ID_KEY, deleted);
    }

    @Override
    @Transactional
    public void markProcessed(Long resourceId) {
        Mp3FileUrl mp3FileUrl = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, resourceId)));

        StorageDto permanent = storageServiceClient.getPermanent();

        s3Service.moveFile(mp3FileUrl.getKey().toString(), mp3FileUrl.getBucket(), permanent.bucket());

        mp3FileUrl.setState(STATE_PERMANENT);
        mp3FileUrl.setBucket(permanent.bucket());
        mp3FileUrl.setPath(permanent.path());

        resourceRepository.save(mp3FileUrl);
    }
}
