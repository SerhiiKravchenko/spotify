package org.epam.learn.resource_service.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.epam.learn.resource_service.client.SongServiceClient;
import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.messaging.ResourceMessagePublisher;
import org.epam.learn.resource_service.model.Mp3FileUrl;
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

    private final ResourceRepository resourceRepository;
    private final S3Service s3Service;
    private final ResourceMessagePublisher messagePublisher;
    private final SongServiceClient songServiceClient;


    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               S3Service s3Service,
                               ResourceMessagePublisher messagePublisher,
                               SongServiceClient songServiceClient) {
        this.resourceRepository = resourceRepository;
        this.s3Service = s3Service;
        this.messagePublisher = messagePublisher;
        this.songServiceClient = songServiceClient;
    }

    @Override
    @Transactional
    public Map<String, Long> upload(byte[] file) {
        Mp3FileUrl mp3FileUrl = s3Service.uploadFile(file);
        Mp3FileUrl savedToDb = resourceRepository.save(mp3FileUrl);
        messagePublisher.publishResourceUploaded(savedToDb.getId());

        return Map.of(SAVED_ID_KEY, savedToDb.getId());
    }

    @Override
    public byte[] download(Long fileId) {
        Utility.isValidId(fileId);

        UUID key = resourceRepository.findById(fileId).map(Mp3FileUrl::getKey)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, fileId)));

        return s3Service.downloadFile(key.toString());
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
            UUID key = resourceRepository.findById(id)
                    .map(Mp3FileUrl::getKey)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, id)));

            resourceRepository.deleteById(id);
            s3Service.deleteFile(key.toString());
        }

        if (!deleted.isEmpty()) {
            songServiceClient.deleteSongs(deleted);
        }

        return Map.of(DELETED_ID_KEY, deleted);
    }
}
