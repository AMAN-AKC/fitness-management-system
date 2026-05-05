package com.fitness.controller;

import com.fitness.dto.NotificationDTO;
import com.fitness.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app and email notifications (US12)")
public class NotificationController {

	private final NotificationService notifService;

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','TRAINER','MANAGER','ADMIN')")
	public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long userId) {
		return ResponseEntity.ok(notifService.getNotificationsForUser(userId));
	}

	@PatchMapping("/{id}/read")
	@ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
	public void markAsRead(@PathVariable Long id) {
		notifService.markAsRead(id);
	}

	@GetMapping("/user/{userId}/unread-count")
	public ResponseEntity<Long> countUnread(@PathVariable Long userId) {
		return ResponseEntity.ok(notifService.countUnread(userId));
	}
}
