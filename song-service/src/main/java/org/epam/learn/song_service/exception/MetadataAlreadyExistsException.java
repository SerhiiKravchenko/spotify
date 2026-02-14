package org.epam.learn.song_service.exception;

public class MetadataAlreadyExistsException extends RuntimeException {

    public MetadataAlreadyExistsException(String message) {
        super(message);
    }
}
