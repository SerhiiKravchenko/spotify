package org.epam.learn.storage_service.mapper;

import org.epam.learn.storage_service.model.Storage;
import org.epam.learn.storage_service.model.StorageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface StorageMapper {

    @Mapping(target = "id", ignore = true)
    Storage toEntity(StorageDto dto);

    StorageDto toDto(Storage storage);
}
