package com.fitness.service;

import com.fitness.config.JwtConfig;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.entity.AuditLog;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

	private final SystemUserRepository userRepo;
	private final JwtConfig jwtConfig;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;

	@Value("${auth.max-failed-attempts:5}")
	private int maxFailedAttempts;

	@Value("${auth.lockout-duration-minutes:15}")
	private int lockoutDurationMinutes;

	public JwtResponse login(LoginRequest req) {
		SystemUser user = userRepo.findByUsername(req.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", req.getUsername()));

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
				int attempts = user.getFailedAttempts() + 1;
				user.setFailedAttempts(attempts);
				if (attempts >= maxFailedAttempts) {
					user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
					user.setFailedAttempts(0);
					log.warn("Account locked due to {} failed attempts: {}", maxFailedAttempts, req.getUsername());
					auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
							AuditLog.Action.LOGIN, "status=unlocked",
							"status=locked, attempts=" + maxFailedAttempts);
				}
				userRepo.save(user);
				log.warn("Failed login attempt for user: {} (attempt #{}/{})",
						req.getUsername(), attempts, maxFailedAttempts);
				throw new BadCredentialsException("Invalid username or password");
			}
		} catch (BadCredentialsException e) {
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error during login for user: {}", req.getUsername(), e);
			throw e;
		}

		user.setFailedAttempts(0);
		user.setLockedUntil(null);
		user.setLastLogin(java.time.LocalDateTime.now());
		userRepo.save(user);

		log.info("Successful login for user: {}", req.getUsername());
		auditLogService.log(user.getUserId(), "SystemUser", user.getUserId(),
				AuditLog.Action.LOGIN, null, "login_successful");

		String token = jwtConfig.generateToken(loadUserByUsername(user.getUsername()));
		return new JwtResponse(token, user.getRole().name(), user.getUserId(), user.getUsername());
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUsername(username)
				.map(u -> org.springframework.security.core.userdetails.User
						.withUsername(u.getUsername())
						.password(u.getPasswordHash())
						.roles(u.getRole().name())
						.accountLocked(u.getLockedUntil() != null &&
								u.getLockedUntil().isAfter(java.time.LocalDateTime.now()))
						.disabled(!u.isActive())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
