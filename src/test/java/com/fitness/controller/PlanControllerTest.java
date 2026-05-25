package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.PlanDTO;
import com.fitness.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanController.class)
public class PlanControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanService planService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlan_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Basic Plan");
        dto.setDurationDays(30);
        dto.setPrice(new java.math.BigDecimal("50.0"));
        dto.setAccessStart("10:00");
        dto.setAccessEnd("22:00");
        dto.setEffectiveFrom("2026-01-01");

        when(planService.createPlan(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planName").value("Basic Plan"));
    }

    @Test
    @WithMockUser
    void getActivePlans_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Basic Plan");

        when(planService.getActivePlans()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/plans")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planName").value("Basic Plan"));
    }

    @Test
    @WithMockUser
    void getPlanById_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Basic Plan");

        when(planService.getPlanById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/plans/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Basic Plan"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePlan_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Updated Plan");
        dto.setDurationDays(30);
        dto.setPrice(new java.math.BigDecimal("50.0"));
        dto.setAccessStart("10:00");
        dto.setAccessEnd("22:00");
        dto.setEffectiveFrom("2026-01-01");

        when(planService.updatePlan(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/plans/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Updated Plan"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivatePlan_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/plans/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(planService).deactivatePlan(1L);
    }
}
