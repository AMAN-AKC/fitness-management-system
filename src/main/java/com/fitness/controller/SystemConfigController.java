package com.fitness.controller;

import com.fitness.entity.ConfigAuditLog;
import com.fitness.entity.FeatureFlag;
import com.fitness.entity.SystemConfig;
import com.fitness.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class SystemConfigController {

	private final SystemConfigService configService;

	@GetMapping("/all")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<List<SystemConfig>> getAllConfigs() {
		return ResponseEntity.ok(configService.getAllConfigs());
	}

	@PutMapping("/update")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateConfigs(@RequestBody Map<String, String> configs) {
		configs.forEach(configService::updateConfig);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/audit")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<ConfigAuditLog>> getAuditLogs() {
		return ResponseEntity.ok(configService.getRecentAudits());
	}

	@GetMapping("/features")
	public ResponseEntity<List<FeatureFlag>> getFeatureFlags() {
		return ResponseEntity.ok(configService.getAllFeatureFlags());
	}

	@GetMapping("/public")
	public ResponseEntity<Map<String, String>> getPublicConfigs() {
		List<SystemConfig> all = configService.getAllConfigs();
		Map<String, String> publicConfigs = new java.util.HashMap<>();
		for (SystemConfig c : all) {
			if (c.getConfigKey().equals("billing.currency") || c.getConfigKey().equals("branding.primaryColor") || c.getConfigKey().equals("branding.logoUrl")) {
				publicConfigs.put(c.getConfigKey(), c.getConfigValue());
			}
		}
		return ResponseEntity.ok(publicConfigs);
	}

	@PutMapping("/features/{name}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> toggleFeature(@PathVariable String name, @RequestParam boolean enabled) {
		configService.toggleFeature(name, enabled);
		return ResponseEntity.ok().build();
	}
}
