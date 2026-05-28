package com.fitness.controller;

import com.fitness.dto.AddOnDTO;
import com.fitness.service.AddOnService;
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

@WebMvcTest(AddOnController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AddOnControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private AddOnService addOnService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAddOn_Success() throws Exception {
        AddOnDTO dto = new AddOnDTO();
        dto.setAddonId(1L);
        dto.setAddonName("Locker");
        dto.setPrice(BigDecimal.valueOf(10));

        when(addOnService.createAddOn(any(AddOnDTO.class))).thenReturn(dto);

        String json = "{\"addonName\":\"Locker\", \"price\":10.0, \"addonType\":\"FACILITY\"}";

        mockMvc.perform(post("/api/v1/addons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addonName").value("Locker"));
    }

    @Test
    void getAllAddOns_Success() throws Exception {
        AddOnDTO dto = new AddOnDTO();
        dto.setAddonId(1L);
        dto.setAddonName("Locker");

        when(addOnService.getAllAddOns()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/addons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addonName").value("Locker"));
    }

    @Test
    void getAddOnById_Success() throws Exception {
        AddOnDTO dto = new AddOnDTO();
        dto.setAddonId(1L);

        when(addOnService.getAddOnById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/addons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addonId").value(1));
    }

    @Test
    void updateAddOn_Success() throws Exception {
        AddOnDTO dto = new AddOnDTO();
        dto.setAddonName("Updated Locker");

        when(addOnService.updateAddOn(eq(1L), any(AddOnDTO.class))).thenReturn(dto);

        String json = "{\"addonName\":\"Updated Locker\", \"price\":15.0, \"addonType\":\"FACILITY\"}";

        mockMvc.perform(put("/api/v1/addons/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addonName").value("Updated Locker"));
    }

    @Test
    void deactivateAddOn_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/addons/1"))
                .andExpect(status().isNoContent());

        verify(addOnService).deactivateAddOn(1L);
    }
}
