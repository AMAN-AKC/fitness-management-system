package com.fitness.service;

import com.fitness.entity.LoginAttempt;
import com.fitness.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository attemptRepo;

    @Value("${auth.max-failed-attempts:5}")
    private int maxAttempts;

    @Value("${auth.ip-lockout-duration-minutes:10}")
    private int lockoutMinutes;

    @Transactional(readOnly = true)
    public void checkIpLockout(String ip) {
        attemptRepo.findByIpAddress(ip).ifPresent(attempt -> {
            if (attempt.isLocked()) {
                throw new RuntimeException("This IP is temporarily locked due to too many failed attempts. Try again later.");
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(String ip) {
        LoginAttempt attempt = attemptRepo.findByIpAddress(ip)
                .orElse(LoginAttempt.builder()
                        .ipAddress(ip)
                        .attempts(0)
                        .build());

        // Reset attempts if the previous lockout has expired
        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isBefore(LocalDateTime.now())) {
            attempt.setAttempts(0);
            attempt.setLockedUntil(null);
        }

        int newAttempts = attempt.getAttempts() + 1;
        attempt.setAttempts(newAttempts);
        attempt.setLastAttempt(LocalDateTime.now());

        if (newAttempts >= maxAttempts) {
            attempt.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            attemptRepo.saveAndFlush(attempt);
            return 0; // 0 attempts left
        }

        attemptRepo.saveAndFlush(attempt);
        return maxAttempts - newAttempts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetAttempts(String ip) {
        attemptRepo.findByIpAddress(ip).ifPresent(attempt -> {
            attempt.setAttempts(0);
            attempt.setLockedUntil(null);
            attemptRepo.saveAndFlush(attempt);
        });
    }
}
