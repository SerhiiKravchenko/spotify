package org.epam.learn.resource_service.repository;

import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends CrudRepository<Mp3FileUrl, Long> {
}
