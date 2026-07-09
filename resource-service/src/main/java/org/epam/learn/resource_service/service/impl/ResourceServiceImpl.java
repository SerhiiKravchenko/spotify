package org.epam.learn.resource_service.service.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.model.MetadataInfo;
import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.epam.learn.resource_service.repository.ResourceRepository;
import org.epam.learn.resource_service.service.MetadataService;
import org.epam.learn.resource_service.service.ResourceService;
import org.epam.learn.resource_service.service.S3Service;
import org.epam.learn.resource_service.utility.Utility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;


@Service
public class ResourceServiceImpl implements ResourceService {

    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource with ID=%d not found";
    private static final String DELETED_ID_KEY = "ids";
    private static final String ID_PATH_PARAMETER = "?id=";
    private static final String COMMA_DELIMITER = ",";
    private static final String SAVED_ID_KEY = "id";

    @Value("${song-service.api.url}")
    private String songsServiceUrl;

    private final ResourceRepository resourceRepository;
    private final MetadataService metadataService;
    private final S3Service s3Service;


    public ResourceServiceImpl(ResourceRepository resourceRepository, MetadataService metadataService, S3Service s3Service) {
        this.resourceRepository = resourceRepository;
        this.metadataService = metadataService;
        this.s3Service = s3Service;
    }

    @Override
    @Transactional
    public Map<String, Long> upload(byte[] file) {
        Mp3FileUrl mp3FileUrl = s3Service.uploadFile(file);
        Mp3FileUrl savedToDb = resourceRepository.save(mp3FileUrl);
        MetadataInfo metadataFromMp3File = metadataService.getMetadataFromMp3File(file);
        metadataFromMp3File.setId(savedToDb.getId());
        sendMetadata(metadataFromMp3File);

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
    public Map<String, List<Long>> delete(List<String> fileIds) {
        Utility.validateStringIdsForDeletion(fileIds);

        List<Long> deleted = fileIds.stream()
                .map(Long::parseLong)
                .filter(resourceRepository::existsById)
                .peek(id -> s3Service.deleteFile(resourceRepository.findById(id).get().getKey().toString()))
                .peek(resourceRepository::deleteById)
                .toList();

        deleteMetadata(deleted);

        return Map.of(DELETED_ID_KEY, deleted);
    }

    private void sendMetadata(MetadataInfo metadataFromMp3File) {
        RestClient restClient = RestClient.create();

        restClient.post()
                .uri(URI.create(songsServiceUrl))
                .body(metadataFromMp3File)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    private void deleteMetadata(List<Long> deleted) {
        if (deleted.isEmpty()) {
            return;
        }

        RestClient restClient = RestClient.create();

        restClient.delete()
                .uri(URI.create(songsServiceUrl + ID_PATH_PARAMETER + String.join(COMMA_DELIMITER, deleted.stream()
                        .map(String::valueOf)
                        .toList())))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }
}
