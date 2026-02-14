package org.epam.learn.song_service.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final String BAD_REQUEST_CODE = "400";
    private static final String AN_UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred: ";
    private static final String INTERNAL_SERVER_ERROR_CODE = "500";
    private static final String INVALID_VALUE_MESSAGE = "Invalid value '%s' for ID. Must be a positive integer";
    private static final String VALIDATION_ERROR_MESSAGE = "Validation error";
    private static final String NOT_FOUND_CODE = "404";
    private static final String CONFLICT_CODE = "409";

    @ExceptionHandler(SongNotFoundException.class)
    public ResponseEntity<Error> handleSongNotFoundException(SongNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage(), NOT_FOUND_CODE));
    }

    @ExceptionHandler(MetadataAlreadyExistsException.class)
    public ResponseEntity<Error> handleMetadataAlreadyExistsException(MetadataAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error(ex.getMessage(), CONFLICT_CODE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Error error = new Error();
        error.setErrorMessage(VALIDATION_ERROR_MESSAGE);
        error.setErrorCode(BAD_REQUEST_CODE);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errors.put(err.getField(), err.getDefaultMessage());
        });
        error.setDetails(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Error> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format(INVALID_VALUE_MESSAGE,
                ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(errorMessage, BAD_REQUEST_CODE));
    }

    @ExceptionHandler(SongIdException.class)
    public ResponseEntity<Error> handleSongIdException(SongIdException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(ex.getMessage(), BAD_REQUEST_CODE));
    }

    @ExceptionHandler(CsvLengthException.class)
    public ResponseEntity<Error> handleCsvLengthException(CsvLengthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(ex.getMessage(), BAD_REQUEST_CODE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Error(AN_UNEXPECTED_ERROR_MESSAGE + ex.getMessage(), INTERNAL_SERVER_ERROR_CODE));
    }
}
