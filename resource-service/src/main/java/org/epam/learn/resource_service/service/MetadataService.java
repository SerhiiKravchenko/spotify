package org.epam.learn.resource_service.service;

import org.epam.learn.resource_service.model.MetadataInfo;

public interface MetadataService {

    MetadataInfo getMetadataFromMp3File(byte[] file);

}
