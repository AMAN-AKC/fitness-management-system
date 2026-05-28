package com.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONFIG_AUDIT_LOG")
@Data
public class ConfigAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "setting_name", nullable = false)
	private String settingName;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "new_value", nullable = false)
	private String newValue;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;

	@Column(name = "timestamp", nullable = false)
	private LocalDateTime timestamp;
}
