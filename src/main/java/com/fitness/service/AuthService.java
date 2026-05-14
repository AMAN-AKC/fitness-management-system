package com.fitness.service;

import com.fitness.config.JwtConfig;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.entity.AuditLog;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.entity.PasswordResetToken;
import com.fitness.repository.PasswordResetTokenRepository;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

	private final SystemUserRepository userRepo;
	private final JwtConfig jwtConfig;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;
	private final PasswordResetTokenRepository passwordResetTokenRepo;
	private final EmailService emailService;
	private final LoginAttemptService loginAttemptService;

	@Value("${auth.max-failed-attempts:5}")
	private int maxFailedAttempts;

	@Value("${auth.lockout-duration-minutes:15}")
	private int lockoutDurationMinutes;

	@Value("${auth.password-regex:^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$}")
	private String passwordRegex;

	public JwtResponse login(LoginRequest req, String ipAddress) {
		// 1. Check IP-level lockout first (AC05)
		loginAttemptService.checkIpLockout(ipAddress);

		SystemUser user = userRepo.findByUsername(req.getUsername())
				.orElseGet(() -> {
					// Even if user not found, record IP attempt to prevent enumeration
					int left = loginAttemptService.recordFailedAttempt(ipAddress);
					throw new BadCredentialsException("Invalid username or password. Attempts left: " + left);
				});

		if (!user.isActive()) {
			log.warn("Login attempt for deactivated account: {}", req.getUsername());
			auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
					AuditLog.Action.LOGIN, "status=active", "status=deactivated");
			throw new BusinessRuleException("Account is deactivated.");
		}

		if (user.getLockedUntil() != null &&
				user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
			log.warn("Login attempt for locked account: {}", req.getUsername());
			throw new BusinessRuleException("Account locked. Try again after " + user.getLockedUntil());
		}

		try {
			String raw = req.getPassword();
			if (raw == null || !passwordEncoder.matches(raw, user.getPasswordHash())) {
				updateFailedAttempts(user);
				int left = loginAttemptService.recordFailedAttempt(ipAddress);
				throw new BadCredentialsException("Invalid username or password. Attempts left: " + left);
			}
		} catch (BadCredentialsException e) {
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error during login for user: {}", req.getUsername(), e);
			throw e;
		}

		resetFailedAttempts(user);
		loginAttemptService.resetAttempts(ipAddress);

		log.info("Successful login for user: {}", req.getUsername());
		auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
				AuditLog.Action.LOGIN, null, "login_successful");

		String token = jwtConfig.generateToken(loadUserByUsername(user.getUsername()));
		return new JwtResponse(token, user.getRole().name(), user.getUserId(), user.getUsername(), user.getFullName());
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUsername(username)
				.map(u -> org.springframework.security.core.userdetails.User
						.withUsername(u.getUsername())
						.password(u.getPasswordHash())
						.roles(u.getRole().name())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	public void requestPasswordReset(String email) {
		SystemUser user = userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

		String token = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP
		PasswordResetToken resetToken = PasswordResetToken.builder()
				.token(token)
				.user(user)
				.expiryDate(java.time.LocalDateTime.now().plusMinutes(5)) // AC08: 5 min expiry
				.build();

		passwordResetTokenRepo.save(resetToken);
		emailService.sendPasswordResetEmail(user.getEmail(), token);

		log.info("Password reset initiated for user: {}", user.getUsername());
		auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
				AuditLog.Action.UPDATE, null, "Password reset initiated");
	}

	public void resetPassword(String token, String newPassword) {
		PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
				.orElseThrow(() -> new BusinessRuleException("Invalid reset token"));

		if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
			throw new BusinessRuleException("Reset token has expired");
		}

		validatePasswordComplexity(newPassword);

		SystemUser user = resetToken.getUser();
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userRepo.save(user);

		passwordResetTokenRepo.delete(resetToken);

		log.info("Password successfully reset for user: {}", user.getUsername());
		auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
				AuditLog.Action.UPDATE, null, "Password reset successful");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateFailedAttempts(SystemUser user) {
		int attempts = user.getFailedAttempts() + 1;
		user.setFailedAttempts(attempts);
		if (attempts >= maxFailedAttempts) {
			user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
			user.setFailedAttempts(0);
			log.warn("Account locked due to {} failed attempts: {}", maxFailedAttempts, user.getUsername());
			auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
					AuditLog.Action.LOGIN, "status=unlocked",
					"status=locked, attempts=" + maxFailedAttempts);
		}
		userRepo.saveAndFlush(user);
		log.warn("Failed login attempt for user: {} (attempt #{}/{})",
				user.getUsername(), attempts, maxFailedAttempts);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void resetFailedAttempts(SystemUser user) {
		user.setFailedAttempts(0);
		user.setLockedUntil(null);
		user.setLastLogin(java.time.LocalDateTime.now());
		userRepo.saveAndFlush(user);
	}

	private void validatePasswordComplexity(String password) {
		if (password == null || !password.matches(passwordRegex)) {
			throw new BusinessRuleException("Password must be at least 8 characters long, " +
					"contain at least one uppercase letter, one lowercase letter, " +
					"one number, and one special character.");
		}
	}
}
