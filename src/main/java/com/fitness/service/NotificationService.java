package com.fitness.service;

import com.fitness.dto.NotificationDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notifRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;

	public NotificationDTO sendNotification(Long userId, Notification.NotifType type,
			Notification.Channel channel,
			String title, String body) {
		SystemUser user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", userId));
		Notification notif = Notification.builder()
				.user(user).type(type).channel(channel)
				.title(title).body(body)
				.isRead(false)
				.deliveryStatus(Notification.DeliveryStatus.SENT)
				.build();
		return mapper.map(notifRepo.save(notif), NotificationDTO.class);
	}

	public List<NotificationDTO> getNotificationsForUser(Long userId) {
		return notifRepo.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
				.map(n -> mapper.map(n, NotificationDTO.class)).collect(Collectors.toList());
	}

	public void markAsRead(Long notifId) {
		Notification notif = notifRepo.findById(notifId)
				.orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notifId));
		notif.setIsRead(true);
		notifRepo.save(notif);
	}

	public long countUnread(Long userId) {
		return notifRepo.findByUserUserIdAndIsReadFalse(userId).size();
	}
}
