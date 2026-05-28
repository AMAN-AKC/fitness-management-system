package com.fitness.service;

import com.fitness.config.JwtConfig;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.entity.PasswordPolicy;
import com.fitness.entity.PasswordResetToken;
import com.fitness.enums.Role;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.PasswordResetTokenRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private SystemUserRepository userRepo;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private PasswordPolicyService passwordPolicyService;
    @Mock
    private PasswordValidationService passwordValidationService;

    private SystemUser mockUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPasswordHash("hashedpassword");
        mockUser.setActive(true);
        mockUser.setRole(Role.ADMIN);
        mockUser.setEmail("test@example.com");
        mockUser.setFailedAttempts(0);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
    }

    @Test
    void login_Success() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(jwtConfig.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");

        JwtResponse response = authService.login(loginRequest, "127.0.0.1");

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(loginAttemptService).resetAttempts("127.0.0.1");
    }

    @Test
    void login_UserNotFound() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        when(loginAttemptService.recordFailedAttempt("127.0.0.1")).thenReturn(4);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, "127.0.0.1"));
    }

    @Test
    void login_DeactivatedAccount() {
        mockUser.setActive(false);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        assertThrows(BusinessRuleException.class, () -> authService.login(loginRequest, "127.0.0.1"));
    }

    @Test
    void login_LockedAccount() {
        mockUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        assertThrows(BusinessRuleException.class, () -> authService.login(loginRequest, "127.0.0.1"));
    }

    @Test
    void login_BadCredentials() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(false);
        when(loginAttemptService.recordFailedAttempt("127.0.0.1")).thenReturn(4);

        com.fitness.dto.PasswordPolicyDto policy = new com.fitness.dto.PasswordPolicyDto();
        policy.setMaxFailedAttempts(5);
        policy.setLockoutDurationMin(15);
        when(passwordPolicyService.getPolicy()).thenReturn(policy);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, "127.0.0.1"));
        assertEquals(1, mockUser.getFailedAttempts());
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        UserDetails userDetails = authService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("unknown"));
    }

    @Test
    void requestPasswordReset_Success() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordResetTokenRepo.save(any())).thenReturn(new PasswordResetToken());

        authService.requestPasswordReset("test@example.com");

        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void resetPassword_Success() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("123456");
        token.setUser(mockUser);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepo.findByToken("123456")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("hashed-new");

        authService.resetPassword("123456", "NewPass1!");

        assertEquals("hashed-new", mockUser.getPasswordHash());
        verify(passwordValidationService).validatePassword("NewPass1!");
        verify(passwordResetTokenRepo).delete(token);
    }
}
