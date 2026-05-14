package com.fitness.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long classId;

	@Column(nullable = false, length = 120)
	private String className;

	@ManyToOne
	@JoinColumn(name = "trainer_id", nullable = false)
	private Trainer trainer;

	@ManyToOne
	@JoinColumn(name = "room_id", nullable = false)
	private Facility room;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false, length = 20)
	private String weekdays;

	@Column(nullable = false)
	private LocalTime classTime;

	@Column(nullable = false)
	private Integer durationMins;

	@Column(nullable = false)
	private Integer capacity;

	@Column(columnDefinition = "TEXT")
	private String prerequisites;

	@Column(length = 255)
	private String planEligibility;

	@Column(nullable = false)
	@jakarta.persistence.Convert(converter = ClassStatusConverter.class)
	@Builder.Default
	private Status status = Status.ACTIVE;

	@Column(columnDefinition = "TEXT")
	private String cancelReason;

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
		ACTIVE, CANCELLED, COMPLETED
	}
}