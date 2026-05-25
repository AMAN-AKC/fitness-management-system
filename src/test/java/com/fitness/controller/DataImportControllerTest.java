package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.BulkImportReport;
import com.fitness.service.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataImportController.class)
public class DataImportControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvImportService csvImportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void importMembersFromCsv_Success() throws Exception {
        BulkImportReport report = BulkImportReport.builder()
                .fileName("test.csv")
                .overallStatus("SUCCESS")
                .summary("Completed")
                .processedAt(LocalDateTime.now())
                .totalRows(1)
                .successCount(1)
                .rowResults(List.of())
                .build();

        when(csvImportService.importMembers(any())).thenReturn(report);

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "name,email".getBytes());

        mockMvc.perform(multipart("/api/v1/import/members/csv")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadImportTemplate_Success() throws Exception {
        mockMvc.perform(get("/api/v1/import/members/template")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"member_import_template.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getImportRules_Success() throws Exception {
        mockMvc.perform(get("/api/v1/import/members/rules")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxFileSize").value("5MB"));
    }
}
