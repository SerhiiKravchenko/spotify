package org.epam.learn.song_service.exception;

public class CsvLengthException extends RuntimeException
{
    public CsvLengthException(String message) {
        super(message);
    }
}
