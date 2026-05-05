package com.fitness.repository;

import com.fitness.entity.Notification;
import com.fitness.entity.Notification.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByUserUserIdOrderByCreatedAtDesc(Long userId);

	List<Notification> findByUserUserIdAndIsReadFalse(Long userId);

	List<Notification> findByDeliveryStatus(DeliveryStatus status);
}
