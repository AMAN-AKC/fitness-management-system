package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long planId;

	@Column(nullable = false, length = 120)
	private String planName;

	@Column(nullable = false)
	private Integer durationDays;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private LocalTime accessStart;

	@Column(nullable = false)
	private LocalTime accessEnd;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private EligibilityType eligibilityType = EligibilityType.GENERAL;

	@Column(length = 50)
	private String prorationRule;

	@Column(precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal taxPercent = BigDecimal.ZERO;

	@Column(nullable = false)
	@Builder.Default
	private Integer version = 1;

	@Column(nullable = false)
	private LocalDate effectiveFrom;

	@Column(length = 255)
	private String branchVisibility;

	@Column(nullable = false)
	@Builder.Default
	private Boolean isActive = true;

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

	public enum EligibilityType {
		GENERAL, STUDENT, SENIOR, CORPORATE
	}
}