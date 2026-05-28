package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_delivery_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false)
	private Notification.Channel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Notification.DeliveryStatus status;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;
}
