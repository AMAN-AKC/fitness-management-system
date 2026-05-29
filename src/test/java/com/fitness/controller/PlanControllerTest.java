package com.fitness.controller;

import com.fitness.dto.PlanDTO;
import com.fitness.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PlanControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PlanService planService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPlan_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanId(1L);
        dto.setPlanName("Gold Plan");

        when(planService.createPlan(any(PlanDTO.class))).thenReturn(dto);

        String json = "{\"planName\":\"Gold Plan\", \"durationDays\":30, \"price\":100.0, \"accessStart\":\"10:00\", \"accessEnd\":\"22:00\", \"effectiveFrom\":\"2024-11-11\"}";

        mockMvc.perform(post("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planName").value("Gold Plan"));
    }

    @Test
    void getActivePlans_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanId(1L);
        dto.setPlanName("Gold Plan");

        when(planService.getActivePlans()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planName").value("Gold Plan"));
    }

    @Test
    void getPlanById_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanId(1L);

        when(planService.getPlanById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(1));
    }

    @Test
    void updatePlan_Success() throws Exception {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Updated Plan");

        when(planService.updatePlan(eq(1L), any(PlanDTO.class))).thenReturn(dto);

        String json = "{\"planName\":\"Updated Plan\", \"durationDays\":30, \"price\":120.0, \"accessStart\":\"10:00\", \"accessEnd\":\"22:00\", \"effectiveFrom\":\"2024-11-11\"}";

        mockMvc.perform(put("/api/v1/plans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Updated Plan"));
    }

    @Test
    void deletePlan_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/plans/1"))
                .andExpect(status().isNoContent());

        verify(planService).deletePlan(1L);
    }
}
