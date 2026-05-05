package com.fitness.validator;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DateNotStartingWithZeroValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DateNotStartingWithZero {
	String message() default "Date/time value must not start with zero";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
