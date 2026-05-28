package com.fitness.controller;

import com.fitness.dto.AuditLogDTO;
import com.fitness.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuditLogControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private AuditLogService auditLogService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllLogs_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(100L);

        when(auditLogService.getAllLogs()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(100));
    }

    @Test
    void getLogsByUser_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(100L);

        when(auditLogService.getLogsByUser(1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/audit-logs/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(100));
    }

    @Test
    void getLogsByEntity_Success() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setAuditId(100L);

        when(auditLogService.getLogsByEntity("Member", 1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/audit-logs/entity")
                .param("entityName", "Member")
                .param("entityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auditId").value(100));
    }
}
