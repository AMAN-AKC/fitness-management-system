package com.fitness.service;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.entity.PasswordPolicy;
import com.fitness.repository.PasswordPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordPolicyServiceTest {

    @InjectMocks
    private PasswordPolicyService passwordPolicyService;

    @Mock
    private PasswordPolicyRepository repository;

    private PasswordPolicy mockPolicy;

    @BeforeEach
    void setUp() {
        mockPolicy = new PasswordPolicy();
        mockPolicy.setPolicyId(1L);
        mockPolicy.setMinPasswordLength(8);
        mockPolicy.setRequireUppercase(true);
        mockPolicy.setRequireNumber(true);
        mockPolicy.setRequireSpecialChar(true);
        mockPolicy.setSessionTimeoutMin(60);
        mockPolicy.setMaxFailedAttempts(5);
        mockPolicy.setLockoutDurationMin(30);
    }

    @Test
    void getPolicy_Existing_ReturnsPolicy() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockPolicy));

        PasswordPolicyDto result = passwordPolicyService.getPolicy();
        
        assertNotNull(result);
        assertEquals(8, result.getMinPasswordLength());
        assertTrue(result.getRequireUppercase());
    }

    @Test
    void getPolicy_NotExisting_CreatesDefault() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(PasswordPolicy.class))).thenReturn(mockPolicy);

        PasswordPolicyDto result = passwordPolicyService.getPolicy();
        
        assertNotNull(result);
        verify(repository).save(any(PasswordPolicy.class));
    }

    @Test
    void updatePolicy_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockPolicy));
        when(repository.save(any(PasswordPolicy.class))).thenReturn(mockPolicy);

        PasswordPolicyDto dto = new PasswordPolicyDto();
        dto.setMinPasswordLength(10);
        dto.setRequireUppercase(true);
        dto.setRequireNumber(true);
        dto.setRequireSpecialChar(true);
        dto.setSessionTimeoutMin(120);
        dto.setMaxFailedAttempts(3);
        dto.setLockoutDurationMin(60);

        PasswordPolicyDto result = passwordPolicyService.updatePolicy(dto, "ADMIN");

        assertNotNull(result);
        verify(repository).save(mockPolicy);
        assertEquals(10, mockPolicy.getMinPasswordLength());
        assertEquals("ADMIN", mockPolicy.getLastUpdatedBy());
    }
}
