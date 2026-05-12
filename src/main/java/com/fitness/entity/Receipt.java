package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long receiptId;

	@Column(nullable = false, unique = true, length = 40)
	private String receiptNumber;

	@ManyToOne
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal taxAmount;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Status status = Status.ISSUED;

	@Column(columnDefinition = "TEXT")
	private String notes;

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
		ISSUED, EMAILED, PRINTED
	}
}
