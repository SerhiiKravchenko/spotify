package org.epam.learn.storage_service.service;

import java.util.List;
import java.util.Map;

import org.epam.learn.storage_service.model.StorageDto;

public interface StorageService {

    Map<String, Long> create(StorageDto storageDto);

    List<StorageDto> getAll();

    List<Long> delete(List<String> ids);
}
