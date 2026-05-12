package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_code_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCodeUsage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "promo_id", nullable = false)
	private PromoCode promoCode;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "invoice_id", nullable = true)
	private Invoice invoice; // null if not yet used for an invoice

	@Column(nullable = false, updatable = false)
	private LocalDateTime usedAt;

	@PrePersist
	protected void onCreate() {
		usedAt = LocalDateTime.now();
	}
}
