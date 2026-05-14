package com.fitness.service;

import com.fitness.dto.AuditLogDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditLogRepository auditRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;
	private final ObjectMapper objectMapper;

	public void log(Long performedById, String entityName, Long entityId,
			AuditLog.Action action, String oldValue, String newValue) {
		SystemUser user = userRepo.findById(performedById)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", performedById));
		
		String jsonOld = formatAsJson(oldValue);
		String jsonNew = formatAsJson(newValue);

		AuditLog log = AuditLog.builder()
				.performedBy(user)
				.entityName(entityName)
				.entityId(entityId)
				.action(action)
				.oldValue(jsonOld)
				.newValue(jsonNew)
				.build();
		auditRepo.save(log);
	}

	private String formatAsJson(String value) {
		if (value == null) return null;
		// If it already looks like JSON (starts with { or [ or is a quoted string), we might want to skip.
		// But to be safe and consistent with the user's request, we'll just ensure it's valid.
		try {
			// Try to parse it to see if it's already valid JSON
			objectMapper.readTree(value);
			return value; // It's already valid JSON
		} catch (Exception e) {
			// Not valid JSON, wrap it as a JSON string
			try {
				return objectMapper.writeValueAsString(value);
			} catch (Exception ex) {
				return "\"" + value.replace("\"", "\\\"") + "\"";
			}
		}
	}

	public List<AuditLogDTO> getAllLogs() {
		return auditRepo.findAllByOrderByCreatedAtDesc().stream()
				.map(l -> {
					AuditLogDTO dto = mapper.map(l, AuditLogDTO.class);
					if (l.getPerformedBy() != null) {
						dto.setUsername(l.getPerformedBy().getUsername());
					}
					dto.setEntity(l.getEntityName());
					dto.setTimestamp(l.getCreatedAt().toString());
					return dto;
				}).collect(Collectors.toList());
	}

	public List<AuditLogDTO> getLogsByUser(Long userId) {
		return auditRepo.findByPerformedByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(l -> mapper.map(l, AuditLogDTO.class)).collect(Collectors.toList());
	}

	public List<AuditLogDTO> getLogsByEntity(String entityName, Long entityId) {
		return auditRepo.findByEntityNameAndEntityId(entityName, entityId).stream()
				.map(l -> mapper.map(l, AuditLogDTO.class)).collect(Collectors.toList());
	}

	/**
	 * Convenience method to log actions for the currently authenticated user.
	 * If no authenticated user is found, attempts to use System user (id=1).
	 */
	public void logForCurrentUser(String entityName, Long entityId, AuditLog.Action action,
			String oldValue, String newValue) {
		String username = null;
		try {
			if (SecurityContextHolder.getContext() != null
					&& SecurityContextHolder.getContext().getAuthentication() != null) {
				username = SecurityContextHolder.getContext().getAuthentication().getName();
			}
		} catch (Exception e) {
			username = null;
		}

		if (username != null) {
			userRepo.findByUsername(username)
					.ifPresent(u -> log(u.getUserId(), entityName, entityId, action, oldValue, newValue));
			return;
		}

		// Fallback to system user id 1 if present
		userRepo.findById(1L).ifPresent(u -> log(u.getUserId(), entityName, entityId, action, oldValue, newValue));
	}
}