package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "add_on")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long addonId;

	@Column(nullable = false, length = 100)
	private String addonName;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	private Integer capacity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AddonType addonType;

	@Column(precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal taxPercent = BigDecimal.ZERO;

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

	public enum AddonType {
		SERVICE, FACILITY, OTHER
	}
}