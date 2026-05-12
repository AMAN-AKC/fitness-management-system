package com.fitness.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordComplexityValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordComplexity {
	String message() default "Password must meet complexity requirements: minimum 12 characters, at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
