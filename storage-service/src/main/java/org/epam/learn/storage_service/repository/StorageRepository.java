package org.epam.learn.storage_service.repository;

import java.util.List;

import org.epam.learn.storage_service.model.Storage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends CrudRepository<Storage, Long> {

    List<Storage> findAll();

    boolean existsByStorageType(String storageType);
}
