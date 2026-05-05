package com.fitness.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "branch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long branchId;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String branchName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String address;

	@NotBlank
	@Column(nullable = false, length = 20)
	private String contact;

	@NotBlank
	@Column(nullable = false, length = 100)
	private String opHours;

	@NotBlank
	@Column(nullable = false, length = 60)
	private String timezone;

	@Column(nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
