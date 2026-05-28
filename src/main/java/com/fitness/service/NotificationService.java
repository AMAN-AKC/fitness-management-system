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
	private final PreferenceManager preferenceManager;
	private final ThrottlingManager throttlingManager;
	private final EmailService emailService;
	private final NotificationDeliveryLogRepository deliveryLogRepo;

	public NotificationDTO sendNotification(Long userId, Notification.NotifType type,
			Notification.Channel channel,
			String title, String body, String deepLink) {
		SystemUser user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", userId));
		Notification notif = Notification.builder()
				.user(user).type(type).channel(channel)
				.title(title).body(body).deepLink(deepLink)
				.isRead(false)
				.deliveryStatus(Notification.DeliveryStatus.PENDING)
				.build();
		return mapper.map(notifRepo.save(notif), NotificationDTO.class);
	}

	public void processNotificationEvent(NotificationEvent event) {
		SystemUser user = event.getUser();
		Notification.NotifType type = event.getType();

		// 1. Process In-App (Bell Center)
		if (preferenceManager.shouldSend(user, Notification.Channel.IN_APP, type)) {
			if (!throttlingManager.isThrottled(user.getUserId(), type)) {
				Notification notif = Notification.builder()
						.user(user).type(type).channel(Notification.Channel.IN_APP)
						.title(event.getFallbackTitle()).body(event.getFallbackBody()).deepLink(event.getDeepLink())
						.isRead(false).deliveryStatus(Notification.DeliveryStatus.DELIVERED)
						.build();
				notifRepo.save(notif);
			}
		}

		// 2. Process Email
		if (preferenceManager.shouldSend(user, Notification.Channel.EMAIL, type)) {
			if (!throttlingManager.isThrottled(user.getUserId(), type)) {
				NotificationDeliveryLog logEntry = new NotificationDeliveryLog();
				logEntry.setChannel(Notification.Channel.EMAIL);
				
				// Create a placeholder Notification record to link the log to
				Notification notifForEmailLog = Notification.builder()
						.user(user).type(type).channel(Notification.Channel.EMAIL)
						.title(event.getFallbackTitle()).body("Email content").deepLink(event.getDeepLink())
						.isRead(false).deliveryStatus(Notification.DeliveryStatus.PENDING)
						.build();
				notifForEmailLog = notifRepo.save(notifForEmailLog);
				logEntry.setNotification(notifForEmailLog);

				try {
					boolean sent = emailService.sendDynamicEmail(
						user.getUsername(), // Using username/email 
						event.getTemplateName(),
						event.getVariables(),
						event.getFallbackTitle(),
						event.getFallbackBody()
					);
					if (sent) {
						logEntry.setStatus(Notification.DeliveryStatus.SENT);
						notifForEmailLog.setDeliveryStatus(Notification.DeliveryStatus.SENT);
					} else {
						logEntry.setStatus(Notification.DeliveryStatus.FAILED);
						logEntry.setFailureReason("Email sending returned false");
						notifForEmailLog.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
					}
				} catch (Exception e) {
					logEntry.setStatus(Notification.DeliveryStatus.FAILED);
					logEntry.setFailureReason(e.getMessage());
					notifForEmailLog.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
				}
				logEntry.setSentAt(java.time.LocalDateTime.now());
				notifRepo.save(notifForEmailLog);
				deliveryLogRepo.save(logEntry);
			}
		}
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
