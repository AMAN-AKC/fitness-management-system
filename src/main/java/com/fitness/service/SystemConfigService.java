package com.fitness.service;

import com.fitness.entity.ConfigAuditLog;
import com.fitness.entity.FeatureFlag;
import com.fitness.entity.SystemConfig;
import com.fitness.repository.ConfigAuditLogRepository;
import com.fitness.repository.FeatureFlagRepository;
import com.fitness.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

	private final SystemConfigRepository configRepo;
	private final ConfigAuditLogRepository auditRepo;
	private final FeatureFlagRepository featureRepo;

	private String getCurrentUsername() {
		try {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		} catch (Exception e) {
			return "SYSTEM";
		}
	}

	@Cacheable(value = "system_config", key = "#key")
	public String getConfigValue(String key, String defaultValue) {
		return configRepo.findByConfigKey(key)
				.map(SystemConfig::getConfigValue)
				.orElse(defaultValue);
	}

	@CacheEvict(value = "system_config", key = "#key")
	public void updateConfig(String key, String newValue) {
		validateConfig(key, newValue);

		SystemConfig config = configRepo.findByConfigKey(key).orElse(new SystemConfig());
		String oldValue = config.getConfigValue();
		
		config.setConfigKey(key);
		config.setConfigValue(newValue);
		config.setUpdatedBy(getCurrentUsername());
		if (config.getVersion() != null) {
			config.setVersion(config.getVersion() + 1);
		}
		
		configRepo.save(config);
		logAudit(key, oldValue, newValue);
	}

	private void validateConfig(String key, String value) {
		if (key.equals("session.timeout")) {
			int timeout = Integer.parseInt(value);
			if (timeout <= 0) {
				throw new IllegalArgumentException("Session timeout must be greater than 0");
			}
		}
		// Add other validations as needed
	}

	private void logAudit(String key, String oldValue, String newValue) {
		ConfigAuditLog logEntry = new ConfigAuditLog();
		logEntry.setSettingName(key);
		logEntry.setOldValue(oldValue);
		logEntry.setNewValue(newValue);
		logEntry.setUpdatedBy(getCurrentUsername());
		logEntry.setTimestamp(LocalDateTime.now());
		auditRepo.save(logEntry);
	}

	// FEATURE FLAGS
	
	@Cacheable(value = "feature_flags", key = "#featureName")
	public boolean isFeatureEnabled(String featureName) {
		return featureRepo.findByFlagName(featureName)
				.map(FeatureFlag::isEnabled)
				.orElse(false); // Default OFF if not found
	}

	@CacheEvict(value = "feature_flags", key = "#featureName")
	public void toggleFeature(String featureName, boolean enabled) {
		if (featureName.equals("BILLING") && !enabled && isFeatureEnabled("DUNNING")) {
			throw new IllegalStateException("Cannot disable BILLING while DUNNING is enabled.");
		}

		FeatureFlag flag = featureRepo.findByFlagName(featureName)
				.orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureName));
		
		String oldVal = String.valueOf(flag.isEnabled());
		flag.setEnabled(enabled);
		flag.setLastModifiedBy(getCurrentUsername());
		featureRepo.save(flag);

		logAudit("FEATURE_" + featureName, oldVal, String.valueOf(enabled));
	}

	public List<FeatureFlag> getAllFeatureFlags() {
		return featureRepo.findAll();
	}
	
	public List<SystemConfig> getAllConfigs() {
		return configRepo.findAll();
	}

	public List<ConfigAuditLog> getRecentAudits() {
		return auditRepo.findTop50ByOrderByTimestampDesc();
	}
}
