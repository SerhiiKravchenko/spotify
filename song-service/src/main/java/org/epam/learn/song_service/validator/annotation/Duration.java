package org.epam.learn.song_service.validator.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.epam.learn.song_service.validator.DurationValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DurationValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Duration {
    String message() default "Duration must be in mm:ss format with leading zeros";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
