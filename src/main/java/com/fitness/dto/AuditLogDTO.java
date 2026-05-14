package com.fitness.dto;

import com.fitness.entity.AuditLog;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
	private Long auditId;
	private Long performedBy;
	private String entityName;
	private Long entityId;
	private AuditLog.Action action;
	private String oldValue;
	private String newValue;
	private String createdAt;
	
	// Virtual fields for frontend
	private String username;
	private String entity;
	private String timestamp;
}