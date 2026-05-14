package com.fitness.controller;

import com.fitness.dto.AuditLogDTO;
import com.fitness.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit trail viewer (Admin/Manager only)")
public class AuditLogController {

	private final AuditLogService auditService;
	
	@GetMapping
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<AuditLogDTO>> getAllLogs() {
		return ResponseEntity.ok(auditService.getAllLogs());
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<AuditLogDTO>> getLogsByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(auditService.getLogsByUser(userId));
	}

	@GetMapping("/entity")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<AuditLogDTO>> getLogsByEntity(@RequestParam String entityName,
			@RequestParam Long entityId) {
		return ResponseEntity.ok(auditService.getLogsByEntity(entityName, entityId));
	}
}