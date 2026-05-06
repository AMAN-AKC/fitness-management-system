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
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@ManyToOne
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amountPaid;

	@Column(nullable = false)
	private LocalDateTime paymentDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

	@Column(length = 255)
	private String failureReason;

	@ManyToOne
	@JoinColumn(name = "refund_by")
	private SystemUser refundBy;

	@Column(columnDefinition = "TEXT")
	private String refundReason;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public enum PaymentMethod {
		CASH, UPI, CARD
	}

	public enum PaymentStatus {
		SUCCESS, FAILED, PENDING, REFUNDED
	}
}