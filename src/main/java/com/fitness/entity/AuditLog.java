package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long auditId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performed_by", nullable = false)
	private SystemUser performedBy;

	@Column(name = "entity_name", nullable = false, length = 60)
	private String entityName;

	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false)
	private Action action;

	@Column(name = "old_value", columnDefinition = "JSON")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "JSON")
	private String newValue;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public enum Action {
		CREATE, UPDATE, DELETE, LOGIN, OVERRIDE
	}
}