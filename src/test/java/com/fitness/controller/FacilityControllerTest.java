package com.fitness.controller;

import com.fitness.dto.FacilityDTO;
import com.fitness.service.FacilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacilityController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FacilityControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private FacilityService facilityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createFacility_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setFacilityId(1L);
        dto.setFacilityName("Yoga Studio");

        when(facilityService.createFacility(any(FacilityDTO.class))).thenReturn(dto);

        String json = "{\"facilityName\":\"Yoga Studio\", \"branchId\":10, \"capacity\":25}";

        mockMvc.perform(post("/api/v1/facilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.facilityName").value("Yoga Studio"));
    }

    @Test
    void getAllFacilities_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setFacilityId(1L);
        dto.setFacilityName("Yoga Studio");

        when(facilityService.getAllFacilities()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityName").value("Yoga Studio"));
    }

    @Test
    void getFacilitiesByBranch_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setFacilityId(1L);

        when(facilityService.getFacilitiesByBranch(10L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/facilities/branch/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facilityId").value(1));
    }

    @Test
    void getFacilityById_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setFacilityId(1L);

        when(facilityService.getFacilityById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/facilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityId").value(1));
    }

    @Test
    void updateFacility_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setFacilityName("Updated Studio");

        when(facilityService.updateFacility(eq(1L), any(FacilityDTO.class))).thenReturn(dto);

        String json = "{\"facilityName\":\"Updated Studio\", \"branchId\":10, \"capacity\":30}";

        mockMvc.perform(put("/api/v1/facilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilityName").value("Updated Studio"));
    }

    @Test
    void deactivateFacility_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/facilities/1"))
                .andExpect(status().isNoContent());

        verify(facilityService).deactivateFacility(1L);
    }

    @Test
    void toggleMaintenance_Success() throws Exception {
        FacilityDTO dto = new FacilityDTO();
        dto.setUnderMaintenance(true);
        dto.setMaintenanceReason("Cleaning");

        when(facilityService.toggleMaintenance(eq(1L), eq(true), eq("Cleaning"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/facilities/1/maintenance")
                .param("underMaintenance", "true")
                .param("reason", "Cleaning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.underMaintenance").value(true));
    }
}
