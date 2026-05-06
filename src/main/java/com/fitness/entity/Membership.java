package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memId;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "plan_id", nullable = false)
	private Plan plan;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Status status = Status.ACTIVE;

	@Column(nullable = false)
	private Integer duration;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column(length = 30)
	private String promoCodeUsed;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

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
		ACTIVE, EXPIRED, PENDING, DUNNING, SUSPENDED
	}
}