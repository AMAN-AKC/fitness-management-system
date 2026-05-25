package com.fitness.service;

import com.fitness.config.JwtConfig;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.entity.PasswordResetToken;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.PasswordResetTokenRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 15);
        ReflectionTestUtils.setField(authService, "passwordRegex", "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("pass");

        SystemUser user = new SystemUser();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setRole(com.fitness.enums.Role.ADMIN);
        user.setPasswordHash("hashed_pass");
        user.setActive(true);

        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);
        when(jwtConfig.generateToken(any())).thenReturn("jwt_token");

        JwtResponse res = authService.login(req, "127.0.0.1");

        assertNotNull(res);
        assertEquals("jwt_token", res.getToken());
        verify(loginAttemptService).resetAttempts("127.0.0.1");
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        SystemUser user = new SystemUser();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setPasswordHash("hashed_pass");
        user.setActive(true);
        user.setFailedAttempts(0);

        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed_pass")).thenReturn(false);
        when(loginAttemptService.recordFailedAttempt("127.0.0.1")).thenReturn(4);

        assertThrows(BadCredentialsException.class, () -> authService.login(req, "127.0.0.1"));
        verify(userRepo).saveAndFlush(user);
    }

    @Test
    void requestPasswordReset_Success() {
        SystemUser user = new SystemUser();
        user.setUserId(1L);
        user.setEmail("admin@test.com");

        when(userRepo.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("admin@test.com");

        verify(passwordResetTokenRepo).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("admin@test.com"), anyString());
    }

    @Test
    void resetPassword_Success() {
        SystemUser user = new SystemUser();
        user.setUserId(1L);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("123456");
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepo.findByToken("123456")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(anyString())).thenReturn("new_hash");

        authService.resetPassword("123456", "Valid1@Pass");

        assertEquals("new_hash", user.getPasswordHash());
        verify(userRepo).save(user);
        verify(passwordResetTokenRepo).delete(resetToken);
    }

    @Test
    void resetPassword_InvalidComplexity_ThrowsException() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("123456");
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepo.findByToken("123456")).thenReturn(Optional.of(resetToken));

        assertThrows(BusinessRuleException.class, () -> authService.resetPassword("123456", "weakpass"));
    }
}
