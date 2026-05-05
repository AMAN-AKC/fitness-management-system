package com.fitness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memberId;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private SystemUser user;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String memName;

	@Email
	@NotBlank
	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@NotBlank
	@Column(nullable = false, unique = true, length = 20)
	private String phone;

	@Column(nullable = false)
	private LocalDate dob;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String address;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String emgContact;

	@NotBlank
	@Column(nullable = false, length = 20)
	private String emgPhone;

	@Column(length = 30)
	private String referralCode;

	@Column(length = 30)
	private String corporateCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.PROSPECT;

	@ManyToOne
	@JoinColumn(name = "home_branch_id", nullable = false)
	private Branch homeBranch;

	@Column(length = 255)
	private String photoPath;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@ManyToOne
	@JoinColumn(name = "created_by", nullable = false)
	private SystemUser createdBy;

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

	public enum Status {
		PROSPECT, ACTIVE, SUSPENDED, DEACTIVATED
	}
}