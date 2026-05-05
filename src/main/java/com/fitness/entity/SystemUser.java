package com.fitness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fitness.enums.Role;

@Entity
@Table(name = "system_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
	private Boolean isActive = true;

	@Column(nullable = false)
	private Integer failedAttempts = 0;

	private LocalDateTime lockedUntil;
	private LocalDateTime lastLogin;
	private LocalDateTime passwordChanged;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

}