package com.fitness.controller;

import com.fitness.entity.ConfigAuditLog;
import com.fitness.entity.FeatureFlag;
import com.fitness.entity.SystemConfig;
import com.fitness.service.SystemConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SystemConfigControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private SystemConfigService configService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllConfigs_Success() throws Exception {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("app.name");

        when(configService.getAllConfigs()).thenReturn(Collections.singletonList(config));

        mockMvc.perform(get("/api/v1/config/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].configKey").value("app.name"));
    }

    @Test
    void updateConfigs_Success() throws Exception {
        String json = "{\"app.name\":\"NewAppName\"}";

        mockMvc.perform(put("/api/v1/config/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
        
        verify(configService).updateConfig("app.name", "NewAppName");
    }

    @Test
    void getAuditLogs_Success() throws Exception {
        ConfigAuditLog auditLog = new ConfigAuditLog();
        auditLog.setSettingName("app.name");

        when(configService.getRecentAudits()).thenReturn(Collections.singletonList(auditLog));

        mockMvc.perform(get("/api/v1/config/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].settingName").value("app.name"));
    }

    @Test
    void getFeatureFlags_Success() throws Exception {
        FeatureFlag flag = new FeatureFlag();
        flag.setFlagName("NEW_UI");

        when(configService.getAllFeatureFlags()).thenReturn(Collections.singletonList(flag));

        mockMvc.perform(get("/api/v1/config/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flagName").value("NEW_UI"));
    }

    @Test
    void toggleFeature_Success() throws Exception {
        mockMvc.perform(put("/api/v1/config/features/NEW_UI/toggle")
                .param("enabled", "true"))
                .andExpect(status().isOk());
        
        verify(configService).toggleFeature("NEW_UI", true);
    }
}
