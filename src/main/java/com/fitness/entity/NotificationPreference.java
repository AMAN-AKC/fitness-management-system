package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long prefId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private SystemUser user;

	@Column(name = "email_enabled", nullable = false)
	@Builder.Default
	private Boolean emailEnabled = true;

	@Column(name = "in_app_enabled", nullable = false)
	@Builder.Default
	private Boolean inAppEnabled = true;

	@Column(name = "promo_enabled", nullable = false)
	@Builder.Default
	private Boolean promoEnabled = true;

	@Column(name = "digest_frequency", nullable = false)
	@Builder.Default
	private String digestFrequency = "WEEKLY"; // DAILY, WEEKLY, NONE

	@Column(name = "quiet_hours_start")
	private LocalTime quietHoursStart; // e.g., 22:00

	@Column(name = "quiet_hours_end")
	private LocalTime quietHoursEnd; // e.g., 07:00

}
