package com.fitness.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DateNotStartingWithZeroValidatorTest {

    private DateNotStartingWithZeroValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new DateNotStartingWithZeroValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_NullValue_ReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_BlankValue_ReturnsTrue() {
        assertTrue(validator.isValid("   ", context));
    }

    @Test
    void isValid_StartsWithZero_ReturnsFalse() {
        assertFalse(validator.isValid("06:00", context));
        assertFalse(validator.isValid("0", context));
    }

    @Test
    void isValid_DoesNotStartWithZero_ReturnsTrue() {
        assertTrue(validator.isValid("10:00", context));
        assertTrue(validator.isValid(" 12:00", context));
    }
}
