package org.epam.learn.storage_service.utility;

import java.util.List;

import org.epam.learn.storage_service.exception.CsvLengthException;
import org.epam.learn.storage_service.exception.IdNotValidException;

public class Utility {

    private static final String ID_REGEX_CHECK = "^[1-9]\\d*$";
    private static final String INVALID_ID_FORMAT_MESSAGE = "Invalid ID format: '%s'. Only positive integers are allowed";
    private static final String CSV_STRING_LENGTH_VIOLATION_MESSAGE = "CSV string is too long: received %d characters, maximum allowed is 200";
    private static final String ID_NOT_NULL_MESSAGE = "ID list cannot be null or empty.";
    private static final int MAX_CSV_LENGTH = 200;

    private Utility() {
    }

    public static void validateStringIdsForDeletion(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(ID_NOT_NULL_MESSAGE);
        }

        ids.forEach(id -> {
            if (!isValidId(id)) {
                throw new IdNotValidException(String.format(INVALID_ID_FORMAT_MESSAGE, id));
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
        if (id == null) {
            return false;
        }
        return id.matches(ID_REGEX_CHECK);
    }
}
