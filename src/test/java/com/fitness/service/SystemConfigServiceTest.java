package com.fitness.service;

import com.fitness.entity.ConfigAuditLog;
import com.fitness.entity.FeatureFlag;
import com.fitness.entity.SystemConfig;
import com.fitness.repository.ConfigAuditLogRepository;
import com.fitness.repository.FeatureFlagRepository;
import com.fitness.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SystemConfigServiceTest {

    @InjectMocks
    private SystemConfigService systemConfigService;

    @Mock
    private SystemConfigRepository configRepo;
    @Mock
    private ConfigAuditLogRepository auditRepo;
    @Mock
    private FeatureFlagRepository featureRepo;

    private SystemConfig mockConfig;
    private FeatureFlag mockFlag;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pass")
        );

        mockConfig = new SystemConfig();
        mockConfig.setConfigKey("app.name");
        mockConfig.setConfigValue("FitnessApp");
        mockConfig.setVersion(1);

        mockFlag = new FeatureFlag();
        mockFlag.setFlagName("NEW_UI");
        mockFlag.setEnabled(true);
    }

    @Test
    void getConfigValue_Exists() {
        when(configRepo.findByConfigKey("app.name")).thenReturn(Optional.of(mockConfig));
        String val = systemConfigService.getConfigValue("app.name", "Default");
        assertEquals("FitnessApp", val);
    }

    @Test
    void getConfigValue_NotFound_ReturnsDefault() {
        when(configRepo.findByConfigKey("app.name")).thenReturn(Optional.empty());
        String val = systemConfigService.getConfigValue("app.name", "Default");
        assertEquals("Default", val);
    }

    @Test
    void updateConfig_Success() {
        when(configRepo.findByConfigKey("app.name")).thenReturn(Optional.of(mockConfig));
        
        systemConfigService.updateConfig("app.name", "NewApp");
        
        assertEquals("NewApp", mockConfig.getConfigValue());
        assertEquals("admin", mockConfig.getUpdatedBy());
        assertEquals(2, mockConfig.getVersion());
        verify(configRepo).save(mockConfig);
        verify(auditRepo).save(any(ConfigAuditLog.class));
    }

    @Test
    void updateConfig_ValidationFailed() {
        assertThrows(IllegalArgumentException.class, () -> systemConfigService.updateConfig("session.timeout", "-1"));
    }

    @Test
    void isFeatureEnabled_True() {
        when(featureRepo.findByFlagName("NEW_UI")).thenReturn(Optional.of(mockFlag));
        assertTrue(systemConfigService.isFeatureEnabled("NEW_UI"));
    }

    @Test
    void isFeatureEnabled_False() {
        when(featureRepo.findByFlagName("UNKNOWN")).thenReturn(Optional.empty());
        assertFalse(systemConfigService.isFeatureEnabled("UNKNOWN"));
    }

    @Test
    void toggleFeature_Success() {
        when(featureRepo.findByFlagName("NEW_UI")).thenReturn(Optional.of(mockFlag));
        
        systemConfigService.toggleFeature("NEW_UI", false);
        
        assertFalse(mockFlag.isEnabled());
        verify(featureRepo).save(mockFlag);
        verify(auditRepo).save(any(ConfigAuditLog.class));
    }

    @Test
    void toggleFeature_IllegalState() {
        FeatureFlag dunningFlag = new FeatureFlag();
        dunningFlag.setFlagName("DUNNING");
        dunningFlag.setEnabled(true);

        when(featureRepo.findByFlagName("DUNNING")).thenReturn(Optional.of(dunningFlag));
        
        assertThrows(IllegalStateException.class, () -> systemConfigService.toggleFeature("BILLING", false));
    }

    @Test
    void toggleFeature_NotFound() {
        when(featureRepo.findByFlagName("UNKNOWN")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> systemConfigService.toggleFeature("UNKNOWN", true));
    }

    @Test
    void getAllFeatureFlags_Success() {
        when(featureRepo.findAll()).thenReturn(Collections.singletonList(mockFlag));
        List<FeatureFlag> results = systemConfigService.getAllFeatureFlags();
        assertEquals(1, results.size());
    }

    @Test
    void getAllConfigs_Success() {
        when(configRepo.findAll()).thenReturn(Collections.singletonList(mockConfig));
        List<SystemConfig> results = systemConfigService.getAllConfigs();
        assertEquals(1, results.size());
    }

    @Test
    void getRecentAudits_Success() {
        when(auditRepo.findTop50ByOrderByTimestampDesc()).thenReturn(Collections.singletonList(new ConfigAuditLog()));
        List<ConfigAuditLog> results = systemConfigService.getRecentAudits();
        assertEquals(1, results.size());
    }
}
