package com.fitness.service;

import com.fitness.entity.Notification;
import com.fitness.entity.NotificationPreference;
import com.fitness.entity.SystemUser;
import com.fitness.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class PreferenceManager {

	private final NotificationPreferenceRepository preferenceRepo;

	public NotificationPreference getPreferences(Long userId) {
		return preferenceRepo.findByUserUserId(userId).orElseGet(() -> {
			NotificationPreference defaultPrefs = new NotificationPreference();
			defaultPrefs.setEmailEnabled(true);
			defaultPrefs.setInAppEnabled(true);
			defaultPrefs.setPromoEnabled(true);
			defaultPrefs.setDigestFrequency("WEEKLY");
			return defaultPrefs;
		});
	}

	public boolean shouldSend(SystemUser user, Notification.Channel channel, Notification.NotifType type) {
		NotificationPreference prefs = getPreferences(user.getUserId());

		// 1. Check channel preference
		if (channel == Notification.Channel.EMAIL && !prefs.getEmailEnabled()) return false;
		if (channel == Notification.Channel.IN_APP && !prefs.getInAppEnabled()) return false;

		// 2. Check type preference
		if (type == Notification.NotifType.GENERAL && !prefs.getPromoEnabled()) return false; // assuming general = promo here

		// 3. Quiet hours (typically only blocks email, but let's block both unless it's CRITICAL)
		// We'll assume BOOKING and DUNNING bypass quiet hours.
		if (type != Notification.NotifType.BOOKING && type != Notification.NotifType.DUNNING) {
			if (inQuietHours(prefs.getQuietHoursStart(), prefs.getQuietHoursEnd())) {
				return false;
			}
		}

		return true;
	}

	private boolean inQuietHours(LocalTime start, LocalTime end) {
		if (start == null || end == null) return false;
		LocalTime now = LocalTime.now();
		if (start.isBefore(end)) {
			return now.isAfter(start) && now.isBefore(end);
		} else {
			// Crosses midnight
			return now.isAfter(start) || now.isBefore(end);
		}
	}
}
