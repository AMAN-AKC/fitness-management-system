package com.fitness.repository;

import com.fitness.entity.NotificationDeliveryLog;
import com.fitness.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {
	
	@Query("SELECT COUNT(l) FROM NotificationDeliveryLog l WHERE l.notification.user.userId = ?1 AND l.notification.type = ?2 AND l.sentAt > ?3 AND l.status = 'SENT'")
	long countSentNotificationsWithinTime(Long userId, Notification.NotifType type, LocalDateTime since);
	
	List<NotificationDeliveryLog> findByStatus(Notification.DeliveryStatus status);
}
