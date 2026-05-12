package com.fitness.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordComplexityValidator implements ConstraintValidator<PasswordComplexity, String> {

	@Value("${password.min-length:12}")
	private int minLength;

	@Value("${password.require-uppercase:true}")
	private boolean requireUppercase;

	@Value("${password.require-lowercase:true}")
	private boolean requireLowercase;

	@Value("${password.require-digit:true}")
	private boolean requireDigit;

	@Value("${password.require-special-char:true}")
	private boolean requireSpecialChar;

	private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:',.<>?/`~";

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		if (password == null) {
			return false;
		}

		// Check minimum length
		if (password.length() < minLength) {
			addConstraintViolation(context, "Password must be at least " + minLength + " characters long.");
			return false;
		}

		boolean valid = true;

		// Check uppercase requirement
		if (requireUppercase && !password.matches(".*[A-Z].*")) {
			addConstraintViolation(context, "Password must contain at least one uppercase letter.");
			valid = false;
		}

		// Check lowercase requirement
		if (requireLowercase && !password.matches(".*[a-z].*")) {
			addConstraintViolation(context, "Password must contain at least one lowercase letter.");
			valid = false;
		}

		// Check digit requirement
		if (requireDigit && !password.matches(".*\\d.*")) {
			addConstraintViolation(context, "Password must contain at least one digit.");
			valid = false;
		}

		// Check special character requirement
		if (requireSpecialChar && !password.matches(".*[" + java.util.regex.Pattern.quote(SPECIAL_CHARS) + "].*")) {
			addConstraintViolation(context, "Password must contain at least one special character.");
			valid = false;
		}

		return valid;
	}

	private void addConstraintViolation(ConstraintValidatorContext context, String message) {
		context.buildConstraintViolationWithTemplate(message)
				.addConstraintViolation()
				.disableDefaultConstraintViolation();
	}
}
