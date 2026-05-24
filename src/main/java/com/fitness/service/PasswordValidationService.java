package com.fitness.service;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordValidationService {

	private final PasswordPolicyService passwordPolicyService;

	private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:',.<>?/`~";

	/**
	 * Validates password complexity according to configured policy.
	 * 
	 * @param password the password to validate
	 * @return true if valid, false otherwise
	 * @throws IllegalArgumentException with detailed message if invalid
	 */
	public void validatePassword(String password) throws BusinessRuleException {
		if (password == null || password.isEmpty()) {
			throw new BusinessRuleException("Password cannot be empty.");
		}
		
		PasswordPolicyDto policy = passwordPolicyService.getPolicy();

		// Check minimum length
		if (password.length() < policy.getMinPasswordLength()) {
			throw new BusinessRuleException(
					"Password must be at least " + policy.getMinPasswordLength() + " characters long.");
		}

		// Check uppercase requirement
		if (policy.getRequireUppercase() && !password.matches(".*[A-Z].*")) {
			throw new BusinessRuleException(
					"Password must contain at least one uppercase letter (A-Z).");
		}

		// Check digit requirement
		if (policy.getRequireNumber() && !password.matches(".*\\d.*")) {
			throw new BusinessRuleException("Password must contain at least one digit (0-9).");
		}

		// Check special character requirement
		if (policy.getRequireSpecialChar()
				&& !password.matches(".*[" + java.util.regex.Pattern.quote(SPECIAL_CHARS) + "].*")) {
			throw new BusinessRuleException(
					"Password must contain at least one special character: " + SPECIAL_CHARS);
		}
	}

	/**
	 * Get the current password policy as a human-readable string.
	 */
	public String getPolicyDescription() {
		PasswordPolicyDto policyDto = passwordPolicyService.getPolicy();
		StringBuilder policy = new StringBuilder("Password must contain: ");
		policy.append("minimum ").append(policyDto.getMinPasswordLength()).append(" characters");

		if (policyDto.getRequireUppercase()) {
			policy.append(", at least 1 uppercase letter");
		}
		if (policyDto.getRequireNumber()) {
			policy.append(", at least 1 digit");
		}
		if (policyDto.getRequireSpecialChar()) {
			policy.append(", at least 1 special character");
		}

		return policy.toString();
	}
}
