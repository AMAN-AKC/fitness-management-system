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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import java.util.Set;
import java.util.HashSet;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
	@Column(name = "full_name", length = 150)
	private String fullName;

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

	@ManyToOne
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToMany
	@JoinTable(
		name = "system_user_branches",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "branch_id")
	)
	@ToString.Exclude
	@Builder.Default
	private Set<Branch> assignedBranches = new HashSet<>();

	public boolean getIsLocked() {
		return lockedUntil != null && java.time.LocalDateTime.now().isBefore(lockedUntil);
	}
}
