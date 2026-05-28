package com.fitness.service;

import com.fitness.entity.LoginAttempt;
import com.fitness.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceTest {

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    @Mock
    private LoginAttemptRepository attemptRepo;

    private LoginAttempt mockAttempt;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", 5);
        ReflectionTestUtils.setField(loginAttemptService, "lockoutMinutes", 10);

        mockAttempt = new LoginAttempt();
        mockAttempt.setId(1L);
        mockAttempt.setIpAddress("127.0.0.1");
        mockAttempt.setAttempts(3);
    }

    @Test
    void checkIpLockout_Success_NotLocked() {
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        assertDoesNotThrow(() -> loginAttemptService.checkIpLockout("127.0.0.1"));
    }

    @Test
    void checkIpLockout_Locked_ThrowsException() {
        mockAttempt.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        
        assertThrows(LockedException.class, () -> loginAttemptService.checkIpLockout("127.0.0.1"));
    }

    @Test
    void recordFailedAttempt_Success() {
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        
        int left = loginAttemptService.recordFailedAttempt("127.0.0.1");
        
        assertEquals(1, left); // 5 - 4
        assertEquals(4, mockAttempt.getAttempts());
        verify(attemptRepo).saveAndFlush(mockAttempt);
    }

    @Test
    void recordFailedAttempt_ReachesLockout() {
        mockAttempt.setAttempts(4);
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        
        int left = loginAttemptService.recordFailedAttempt("127.0.0.1");
        
        assertEquals(0, left);
        assertEquals(5, mockAttempt.getAttempts());
        assertNotNull(mockAttempt.getLockedUntil());
        verify(attemptRepo).saveAndFlush(mockAttempt);
    }

    @Test
    void recordFailedAttempt_ExpiredLockout_Resets() {
        mockAttempt.setAttempts(5);
        mockAttempt.setLockedUntil(LocalDateTime.now().minusMinutes(5)); // expired 5 mins ago
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        
        int left = loginAttemptService.recordFailedAttempt("127.0.0.1");
        
        assertEquals(4, left); // Reset to 0, then +1 = 1 attempt. 5 - 1 = 4 left.
        assertEquals(1, mockAttempt.getAttempts());
        assertNull(mockAttempt.getLockedUntil());
        verify(attemptRepo).saveAndFlush(mockAttempt);
    }

    @Test
    void resetAttempts_Success() {
        when(attemptRepo.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(mockAttempt));
        
        loginAttemptService.resetAttempts("127.0.0.1");
        
        assertEquals(0, mockAttempt.getAttempts());
        assertNull(mockAttempt.getLockedUntil());
        verify(attemptRepo).saveAndFlush(mockAttempt);
    }
}
