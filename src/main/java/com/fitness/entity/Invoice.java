package com.fitness.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long invoiceId;

	@Column(nullable = false, unique = true, length = 40)
	private String invoiceNumber;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "membership_id")
	private Membership membership;

	@Column(length = 100)
	private String planName;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal mrp;

	@Column(precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal taxes = BigDecimal.ZERO;

	@Column(precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal discount = BigDecimal.ZERO;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal finalAmount;

	@Column(precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal paidAmount = BigDecimal.ZERO;

	@Column(precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal outstanding = BigDecimal.ZERO;

	@Column(length = 30)
	private String promoCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Status status = Status.DRAFT;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public enum Status {
		DRAFT, ISSUED, PAID, OVERDUE, VOID, PENDING, UNPAID
	}
}