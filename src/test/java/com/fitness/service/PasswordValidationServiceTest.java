package com.fitness.service;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordValidationServiceTest {

    @InjectMocks
    private PasswordValidationService passwordValidationService;

    @Mock
    private PasswordPolicyService passwordPolicyService;

    private PasswordPolicyDto policy;

    @BeforeEach
    void setUp() {
        policy = new PasswordPolicyDto();
        policy.setMinPasswordLength(8);
        policy.setRequireUppercase(true);
        policy.setRequireNumber(true);
        policy.setRequireSpecialChar(true);
    }

    @Test
    void validatePassword_Success() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        assertDoesNotThrow(() -> passwordValidationService.validatePassword("ValidPass1!"));
    }

    @Test
    void validatePassword_EmptyPassword() {
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword(""));
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword(null));
    }

    @Test
    void validatePassword_TooShort() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword("Short1!"));
    }

    @Test
    void validatePassword_NoUppercase() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword("noupper1!"));
    }

    @Test
    void validatePassword_NoDigit() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword("NoDigit!!"));
    }

    @Test
    void validatePassword_NoSpecialChar() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        assertThrows(BusinessRuleException.class, () -> passwordValidationService.validatePassword("NoSpecial123"));
    }

    @Test
    void getPolicyDescription_Success() {
        when(passwordPolicyService.getPolicy()).thenReturn(policy);
        String desc = passwordValidationService.getPolicyDescription();
        assertTrue(desc.contains("minimum 8 characters"));
        assertTrue(desc.contains("uppercase"));
        assertTrue(desc.contains("digit"));
        assertTrue(desc.contains("special character"));
    }
}
