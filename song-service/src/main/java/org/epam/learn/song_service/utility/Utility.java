package org.epam.learn.song_service.utility;

import java.util.List;

import org.epam.learn.song_service.exception.CsvLengthException;
import org.epam.learn.song_service.exception.SongIdException;

public class Utility {

    private static final String INVALID_ID_VALUE_MESSAGE = "Invalid value '%d' for ID. Must be a positive integer";
    private static final String ID_LIST_CANNOT_BE_EMPTY_MESSAGE = "ID list cannot be null or empty.";
    private static final String INVALID_ID_FORMAT_MESSAGE = "Invalid ID format: '%s'. Only positive integers are allowed";
    private static final String CSV_STRING_LENGTH_VIOLATION_MESSAGE = "CSV string is too long: received %d characters, maximum allowed is 200";
    private static final String ID_VALIDATION_REGEX = "^[1-9]\\d*$";
    private static final int MAX_CSV_LENGTH = 200;

    public static void validateIdIsPositive(Long id) {
        if (id == null || id <= 0) {
            throw new SongIdException(String.format(INVALID_ID_VALUE_MESSAGE, id));
        }
    }

    public static void validateStringIdsForDeletion(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(ID_LIST_CANNOT_BE_EMPTY_MESSAGE);
        }

        ids.forEach(id -> {
            if (!isValidId(id)) {
                throw new SongIdException(String.format(INVALID_ID_FORMAT_MESSAGE, id));
            }
        });

        checkForCSVRestriction(ids);
    }

    private static void checkForCSVRestriction(List<String> ids) {
        int count = 0;
        for (int i = 0; i < ids.size(); i++) {
            count += ids.get(i).length();
            if (i != ids.size() - 1) {
                count++;
            }
        }
        if (count >= MAX_CSV_LENGTH) {
            String message = String.format(CSV_STRING_LENGTH_VIOLATION_MESSAGE, count);
            throw new CsvLengthException(message);
        }
    }

    private static boolean isValidId(String id) {
        if (id == null) return false;

        return id.matches(ID_VALIDATION_REGEX);
    }
}
