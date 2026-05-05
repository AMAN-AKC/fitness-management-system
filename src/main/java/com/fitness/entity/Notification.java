package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notifId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private SystemUser user;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private NotifType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false)
	private Channel channel;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "body", nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private Boolean isRead = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "delivery_status", nullable = false)
	@Builder.Default
	private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public enum NotifType {
		BOOKING, CANCELLATION, RENEWAL, SCHEDULE_CHANGE, DUNNING, CHECK_IN, GENERAL
	}

	public enum Channel {
		IN_APP, EMAIL
	}

	public enum DeliveryStatus {
		SENT, FAILED, PENDING
	}
}