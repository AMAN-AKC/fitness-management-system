package com.fitness.dto;

import com.fitness.entity.Notification;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
	private Long notifId;
	private Long userId;
	private Notification.NotifType type;
	private Notification.Channel channel;
	private String title;
	private String body;
	private Boolean isRead;
	private Notification.DeliveryStatus deliveryStatus;
	private String createdAt;
}
