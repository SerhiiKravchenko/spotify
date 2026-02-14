package org.epam.learn.resource_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Error> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage(), "404"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Error> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String message = String.format("Invalid file format: %s. Only MP3 files are allowed", ex.getContentType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(message, "400"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Error> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for ID. Must be a positive integer", ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(message, "400"));
    }

    @ExceptionHandler(IdNotValidException.class)
    public ResponseEntity<Error> handleIdNotValidException(IdNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(ex.getMessage(), "400"));
    }

    @ExceptionHandler(CsvLengthException.class)
    public ResponseEntity<Error> handleCsvLengthException(CsvLengthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(ex.getMessage(), "400"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Error("An unexpected error occurred: " + ex.getMessage(), "500"));
    }
}
