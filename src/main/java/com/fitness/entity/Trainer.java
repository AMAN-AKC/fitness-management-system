package com.fitness.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trainer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long trainerId;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private SystemUser user;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@Column(columnDefinition = "TEXT")
	private String bio;

	@Column(length = 255)
	private String certifications;

	@Column(length = 255)
	private String specialties;

	@Column(nullable = false)
	@Builder.Default
	private Double rating = 0.0;

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
}