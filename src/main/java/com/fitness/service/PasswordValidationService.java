package com.fitness.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {

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

	/**
	 * Validates password complexity according to configured policy.
	 * 
	 * @param password the password to validate
	 * @return true if valid, false otherwise
	 * @throws IllegalArgumentException with detailed message if invalid
	 */
	public void validatePassword(String password) throws IllegalArgumentException {
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("Password cannot be empty.");
		}

		// Check minimum length
		if (password.length() < minLength) {
			throw new IllegalArgumentException(
					"Password must be at least " + minLength + " characters long.");
		}

		// Check uppercase requirement
		if (requireUppercase && !password.matches(".*[A-Z].*")) {
			throw new IllegalArgumentException(
					"Password must contain at least one uppercase letter (A-Z).");
		}

		// Check lowercase requirement
		if (requireLowercase && !password.matches(".*[a-z].*")) {
			throw new IllegalArgumentException(
					"Password must contain at least one lowercase letter (a-z).");
		}

		// Check digit requirement
		if (requireDigit && !password.matches(".*\\d.*")) {
			throw new IllegalArgumentException("Password must contain at least one digit (0-9).");
		}

		// Check special character requirement
		if (requireSpecialChar
				&& !password.matches(".*[" + java.util.regex.Pattern.quote(SPECIAL_CHARS) + "].*")) {
			throw new IllegalArgumentException(
					"Password must contain at least one special character: " + SPECIAL_CHARS);
		}
	}

	/**
	 * Get the current password policy as a human-readable string.
	 */
	public String getPolicyDescription() {
		StringBuilder policy = new StringBuilder("Password must contain: ");
		policy.append("minimum ").append(minLength).append(" characters");

		if (requireUppercase) {
			policy.append(", at least 1 uppercase letter");
		}
		if (requireLowercase) {
			policy.append(", at least 1 lowercase letter");
		}
		if (requireDigit) {
			policy.append(", at least 1 digit");
		}
		if (requireSpecialChar) {
			policy.append(", at least 1 special character");
		}

		return policy.toString();
	}
}
