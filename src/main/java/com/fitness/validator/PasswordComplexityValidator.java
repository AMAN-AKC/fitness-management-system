package com.fitness.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fitness.service.PasswordValidationService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class PasswordComplexityValidator implements ConstraintValidator<PasswordComplexity, String> {

	@Autowired
	private PasswordValidationService passwordValidationService;

	private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:',.<>?/`~";

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		if (password == null) {
			return false;
		}

		try {
			passwordValidationService.validatePassword(password);
			return true;
		} catch (Exception e) {
			addConstraintViolation(context, e.getMessage());
			return false;
		}
	}

	private void addConstraintViolation(ConstraintValidatorContext context, String message) {
		context.buildConstraintViolationWithTemplate(message)
				.addConstraintViolation()
				.disableDefaultConstraintViolation();
	}
}
