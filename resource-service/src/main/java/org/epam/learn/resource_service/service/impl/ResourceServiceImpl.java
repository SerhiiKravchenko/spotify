package org.epam.learn.resource_service.service.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.model.MetadataInfo;
import org.epam.learn.resource_service.model.Mp3File;
import org.epam.learn.resource_service.repository.ResourceRepository;
import org.epam.learn.resource_service.service.MetadataService;
import org.epam.learn.resource_service.service.ResourceService;
import org.epam.learn.resource_service.utility.Utility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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


    public ResourceServiceImpl(ResourceRepository resourceRepository, MetadataService metadataService) {
        this.resourceRepository = resourceRepository;
        this.metadataService = metadataService;
    }

    @Override
    @Transactional
    public Map<String, Long> upload(byte[] file) {
        Mp3File mp3File = new Mp3File(file);
        Mp3File saved = resourceRepository.save(mp3File);
        MetadataInfo metadataFromMp3File = metadataService.getMetadataFromMp3File(file);
        metadataFromMp3File.setId(saved.getId());
        sendMetadata(metadataFromMp3File);

        return Map.of(SAVED_ID_KEY, saved.getId());
    }

    @Override
    public byte[] download(Long fileId) {
        Utility.isValidId(fileId);

        return resourceRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_MESSAGE, fileId)))
                .getFile();
    }

    @Override
    public Map<String, List<Long>> delete(List<String> fileIds) {
        Utility.validateStringIdsForDeletion(fileIds);

        List<Long> deleted = fileIds.stream()
                .map(Long::parseLong)
                .filter(resourceRepository::existsById)
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

        ResponseEntity<Map<String, List<Long>>> ids = restClient.delete()
                .uri(URI.create(songsServiceUrl + ID_PATH_PARAMETER + String.join(COMMA_DELIMITER, deleted.stream()
                        .map(String::valueOf)
                        .toList())))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }
}
