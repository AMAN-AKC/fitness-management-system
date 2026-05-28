package com.fitness.controller;

import com.fitness.entity.FeatureFlag;
import com.fitness.service.FeatureFlagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeatureFlagController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FeatureFlagControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private FeatureFlagService featureFlagService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllFlags_Success() throws Exception {
        FeatureFlag flag = new FeatureFlag();
        flag.setFlagId(1L);
        flag.setFlagName("NEW_UI");
        flag.setEnabled(true);

        when(featureFlagService.getAllFlags()).thenReturn(Collections.singletonList(flag));

        mockMvc.perform(get("/api/v1/admin/feature-flags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flagId").value(1));
    }

    @Test
    void updateFlag_Success() throws Exception {
        FeatureFlag flag = new FeatureFlag();
        flag.setFlagId(10L);

        when(featureFlagService.updateFlag(eq(10L), anyBoolean(), anyString())).thenReturn(flag);

        mockMvc.perform(put("/api/v1/admin/feature-flags/10")
                .param("enabled", "true")
                .param("modifiedBy", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagId").value(10));
    }
}
