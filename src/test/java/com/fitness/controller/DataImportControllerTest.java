package com.fitness.controller;

import com.fitness.dto.BulkImportReport;
import com.fitness.dto.BulkImportRowResult;
import com.fitness.service.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataImportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataImportControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private CsvImportService csvImportService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void importMembersFromCsv_Success() throws Exception {
        BulkImportReport report = BulkImportReport.builder()
                .fileName("members.csv")
                .overallStatus("SUCCESS")
                .successCount(1)
                .totalRows(1)
                .rowResults(new ArrayList<>())
                .build();

        when(csvImportService.importMembers(any())).thenReturn(report);

        MockMultipartFile file = new MockMultipartFile("file", "members.csv", "text/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/import/members/csv")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.overallStatus").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadImportTemplate_Success() throws Exception {
        mockMvc.perform(get("/api/v1/import/members/template")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"member_import_template.csv\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("memName,email,phone")));
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
