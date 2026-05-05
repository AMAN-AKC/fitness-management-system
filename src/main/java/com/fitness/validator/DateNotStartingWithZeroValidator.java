package com.fitness.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateNotStartingWithZeroValidator implements ConstraintValidator<DateNotStartingWithZero, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext ctx) {
		if (value == null || value.isBlank())
			return true; // handled by @NotBlank
		// Reject if any numeric segment (year, month, day, hour, minute, second) starts
		// with 0
		// Works for: "yyyy-MM-dd", "HH:mm", "yyyy-MM-ddTHH:mm:ss"
		String[] parts = value.split("[-T:.]");
		for (String part : parts) {
			if (part.length() > 1 && part.startsWith("0")) {
				return false;
			}
		}
		return true;
	}
}