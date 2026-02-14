package org.epam.learn.song_service.validator;

import org.epam.learn.song_service.validator.annotation.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class YearValidator implements ConstraintValidator<Year, String> {

    private static final int MIN_POSSIBLE_YEAR = 1900;
    private static final int MAX_POSSIBLE_YEAR = 2099;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s.length() != 4) {
            return false;
        }

        try {
            int year = Integer.parseInt(s);
            return year >= MIN_POSSIBLE_YEAR && year <= MAX_POSSIBLE_YEAR;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
