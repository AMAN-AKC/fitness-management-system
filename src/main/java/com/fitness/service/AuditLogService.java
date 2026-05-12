package com.fitness.service;

import com.fitness.dto.AuditLogDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditLogRepository auditRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;

	public void log(Long performedById, String entityName, Long entityId,
			AuditLog.Action action, String oldValue, String newValue) {
		SystemUser user = userRepo.findById(performedById)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", performedById));
		AuditLog log = AuditLog.builder()
				.performedBy(user)
				.entityName(entityName)
				.entityId(entityId)
				.action(action)
				.oldValue(oldValue)
				.newValue(newValue)
				.build();
		auditRepo.save(log);
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