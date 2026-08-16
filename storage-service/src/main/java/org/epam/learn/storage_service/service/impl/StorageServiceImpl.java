package org.epam.learn.storage_service.service.impl;

import java.util.List;
import java.util.Map;

import org.epam.learn.storage_service.mapper.StorageMapper;
import org.epam.learn.storage_service.model.Storage;
import org.epam.learn.storage_service.model.StorageDto;
import org.epam.learn.storage_service.repository.StorageRepository;
import org.epam.learn.storage_service.service.StorageService;
import org.epam.learn.storage_service.utility.Utility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageServiceImpl implements StorageService {

    private static final String SAVED_ID_KEY = "id";

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;

    public StorageServiceImpl(StorageRepository storageRepository, StorageMapper storageMapper) {
        this.storageRepository = storageRepository;
        this.storageMapper = storageMapper;
    }

    @Override
    public Map<String, Long> create(StorageDto storageDto) {
        Storage saved = storageRepository.save(storageMapper.toEntity(storageDto));
        return Map.of(SAVED_ID_KEY, saved.getId());
    }

    @Override
    public List<StorageDto> getAll() {
        return storageRepository.findAll().stream()
                .map(storageMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public List<Long> delete(List<String> ids) {
        Utility.validateStringIdsForDeletion(ids);

        List<Long> existingIds = ids.stream()
                .map(Long::parseLong)
                .filter(storageRepository::existsById)
                .toList();

        existingIds.forEach(storageRepository::deleteById);

        return existingIds;
    }
}
