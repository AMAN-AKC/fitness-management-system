package com.fitness.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_booking", uniqueConstraints = { @UniqueConstraint(columnNames = { "class_id", "member_id" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bookingId;

	@ManyToOne
	@JoinColumn(name = "class_id", nullable = false)
	private Classes fitnessClass;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

	private Integer waitlistPosition;
	private LocalDateTime cancelledAt;

	@ManyToOne
	@JoinColumn(name = "override_by")
	private SystemUser overrideBy;

	@Column(columnDefinition = "TEXT")
	private String overrideReason;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public enum BookingStatus {
		CONFIRMED, WAITLISTED, CANCELLED, NO_SHOW
	}
}