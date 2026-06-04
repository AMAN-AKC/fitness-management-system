package com.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SYSTEM_CONFIG")
@Data
public class SystemConfig {

	@Id
	@Column(name = "config_key", nullable = false, unique = true)
	private String configKey;

	@Column(name = "config_value", nullable = false, columnDefinition = "LONGTEXT")
	private String configValue;

	@Column(name = "version", nullable = false)
	private Integer version = 1;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	@PrePersist
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
