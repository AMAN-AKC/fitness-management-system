package com.fitness.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DateNotStartingWithZeroValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DateNotStartingWithZero {
	String message() default "Invalid format selection: Date and time values must not start with zero. Please utilize the calendar dropdown selector tool.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
