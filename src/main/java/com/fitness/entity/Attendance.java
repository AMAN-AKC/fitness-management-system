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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@Column(nullable = false)
	private LocalDateTime checkInTime;

	private LocalDateTime checkOutTime;

	@Column(nullable = false)
	@Builder.Default
	private Boolean alertFlag = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScanMethod scanMethod;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SyncStatus syncStatus = SyncStatus.SYNCED;

	@ManyToOne
	@JoinColumn(name = "class_id")
	private Classes fitnessClass;

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

	public enum ScanMethod {
		QR, CARD, MANUAL
	}

	public enum SyncStatus {
		SYNCED, PENDING
	}
}