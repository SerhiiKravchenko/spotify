package org.epam.learn.resourceprocessor.service;

import org.epam.learn.resourceprocessor.model.SongMetadata;

public interface MetadataExtractorService {
    SongMetadata extract(byte[] mp3File);
}
