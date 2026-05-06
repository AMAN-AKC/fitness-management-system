package com.fitness.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promo_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long promoId;

	@Column(nullable = false, unique = true, length = 30)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiscountType discountType;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal discountValue;

	@Column(nullable = false)
	private LocalDate expiryDate;

	@Column(nullable = false)
	private Integer usageLimit;

	@Column(nullable = false)
	@Builder.Default
	private Integer perMemberLimit = 1;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private Eligibility eligibility = Eligibility.ALL;

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

	public enum DiscountType {
		PERCENT, FIXED
	}

	public enum Eligibility {
		ALL, NEW, RETURNING
	}
}