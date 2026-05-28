package com.fitness.controller;

import com.fitness.entity.EmailTemplate;
import com.fitness.entity.NotificationDeliveryLog;
import com.fitness.repository.EmailTemplateRepository;
import com.fitness.repository.NotificationDeliveryLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notifications", description = "Admin endpoints for templates and delivery logs")
public class NotificationAdminController {

	private final EmailTemplateRepository templateRepo;
	private final NotificationDeliveryLogRepository deliveryLogRepo;

	@PostMapping("/templates")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
		return ResponseEntity.ok(templateRepo.save(template));
	}

	@GetMapping("/templates")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
		return ResponseEntity.ok(templateRepo.findAll());
	}

	@GetMapping("/delivery-logs")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<NotificationDeliveryLog>> getFailedLogs() {
		return ResponseEntity.ok(deliveryLogRepo.findByStatus(com.fitness.entity.Notification.DeliveryStatus.FAILED));
	}
}
