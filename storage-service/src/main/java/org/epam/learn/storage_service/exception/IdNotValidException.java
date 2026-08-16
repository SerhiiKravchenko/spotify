package org.epam.learn.storage_service.exception;

public class IdNotValidException extends RuntimeException {

    public IdNotValidException(String message) {
        super(message);
    }
}
