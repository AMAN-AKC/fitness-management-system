package com.fitness.controller;

import com.fitness.dto.AuditLogDTO;
import com.fitness.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
public class AuditLogControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllLogs_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(1L);

        when(auditLogService.getAllLogs()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/audit-logs")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogsByUser_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(1L);

        when(auditLogService.getLogsByUser(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/audit-logs/user/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogsByEntity_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(1L);

        when(auditLogService.getLogsByEntity("Member", 1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/audit-logs/entity")
                .param("entityName", "Member")
                .param("entityId", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(1L));
    }
}
