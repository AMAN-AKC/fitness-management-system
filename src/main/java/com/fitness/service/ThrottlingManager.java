package com.fitness.service;

import com.fitness.entity.Notification;
import com.fitness.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ThrottlingManager {

	private final NotificationDeliveryLogRepository deliveryLogRepo;

	public boolean isThrottled(Long userId, Notification.NotifType type) {
		LocalDateTime now = LocalDateTime.now();

		if (type == Notification.NotifType.GENERAL) {
			// Max 3 promo/general per day
			long count = deliveryLogRepo.countSentNotificationsWithinTime(userId, type, now.minusDays(1));
			if (count >= 3) return true;
		} else if (type == Notification.NotifType.RENEWAL) {
			// Max 1 renewal reminder per day
			long count = deliveryLogRepo.countSentNotificationsWithinTime(userId, type, now.minusDays(1));
			if (count >= 1) return true;
		}

		// By default, not throttled
		return false;
	}
}
