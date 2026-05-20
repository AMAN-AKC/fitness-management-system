package com.fitness.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateNotStartingWithZeroValidator implements ConstraintValidator<DateNotStartingWithZero, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext ctx) {
		if (value == null || value.isBlank())
			return true; 
		
		return !value.trim().startsWith("0");
	}
}