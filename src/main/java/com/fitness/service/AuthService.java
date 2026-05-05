package com.fitness.service;

import com.fitness.config.JwtConfig;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

	private final SystemUserRepository userRepo;
	private final JwtConfig jwtConfig;
	private final AuthenticationManager authManager;
	private final PasswordEncoder passwordEncoder;
	private static final int MAX_ATTEMPTS = 5;

	public JwtResponse login(LoginRequest req) {
		SystemUser user = userRepo.findByUsername(req.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", req.getUsername()));

		if (!user.getIsActive())
			throw new BusinessRuleException("Account is deactivated.");

		if (user.getLockedUntil() != null &&
				user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
			throw new BusinessRuleException("Account locked. Try again after " + user.getLockedUntil());
		}

		try {
			authManager.authenticate(
					new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
		} catch (Exception e) {
			int attempts = user.getFailedAttempts() + 1;
			user.setFailedAttempts(attempts);
			if (attempts >= MAX_ATTEMPTS) {
				user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
				user.setFailedAttempts(0);
			}
			userRepo.save(user);
			throw e;
		}

		user.setFailedAttempts(0);
		user.setLockedUntil(null);
		user.setLastLogin(java.time.LocalDateTime.now());
		userRepo.save(user);

		String token = jwtConfig.generateToken(loadUserByUsername(user.getUsername()));
		return new JwtResponse(token, user.getRole().name(), user.getUserId());
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
						.disabled(!u.getIsActive())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
