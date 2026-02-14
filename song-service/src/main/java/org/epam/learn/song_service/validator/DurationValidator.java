package org.epam.learn.song_service.validator;

import org.epam.learn.song_service.validator.annotation.Duration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DurationValidator implements ConstraintValidator<Duration, String> {

    private static final String DURATION_FORMAT_REGEX = "^(\\d{2}):(0[0-5]|[0-5]\\d)$";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s.matches(DURATION_FORMAT_REGEX);
    }
}
