package org.epam.learn.resource_service.service;

import java.util.List;
import java.util.Map;

public interface ResourceService {

    Map<String, Long> upload(byte[] file);

    byte[] download(Long fileId);

    Map<String, List<Long>> delete(List<String> fileIds);

    void markProcessed(Long resourceId);
}
