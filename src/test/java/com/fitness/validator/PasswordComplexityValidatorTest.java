package com.fitness.validator;

import com.fitness.exception.ValidationException;
import com.fitness.service.PasswordValidationService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordComplexityValidatorTest {

    @InjectMocks
    private PasswordComplexityValidator validator;

    @Mock
    private PasswordValidationService passwordValidationService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
    }

    @Test
    void isValid_NullPassword_ReturnsFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void isValid_ValidPassword_ReturnsTrue() {
        doNothing().when(passwordValidationService).validatePassword("StrongPass1!");
        assertTrue(validator.isValid("StrongPass1!", context));
    }

    @Test
    void isValid_InvalidPassword_ReturnsFalse() {
        doThrow(new ValidationException("Weak password"))
                .when(passwordValidationService).validatePassword("weak");

        when(context.buildConstraintViolationWithTemplate("Weak password")).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        assertFalse(validator.isValid("weak", context));
        verify(context).disableDefaultConstraintViolation();
    }
}
