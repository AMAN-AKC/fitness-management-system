package com.fitness.entity;

import com.fitness.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	@NotBlank
	@Column(nullable = false, unique = true, length = 80)
	private String username;

	@Email
	@NotBlank
	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@NotBlank
	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	@Builder.Default
	private boolean active = true;

	@Column(name = "locked_until")
	private java.time.LocalDateTime lockedUntil;

	@Column(name = "failed_attempts")
	@Builder.Default
	private int failedAttempts = 0;

	@Column(name = "last_login")
	private java.time.LocalDateTime lastLogin;

}