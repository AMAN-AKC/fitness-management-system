package com.fitness.controller;

import com.fitness.dto.ManagerDashboardDto;
import com.fitness.entity.Invoice;
import com.fitness.repository.InvoiceRepository;
import com.fitness.service.ManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ManagerControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private ManagerService managerService;
    @MockBean
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardStats_Success() throws Exception {
        ManagerDashboardDto dto = new ManagerDashboardDto();
        dto.setActiveMembers(500L);
        dto.setMonthlyRevenue(15000.0);

        when(managerService.getDashboardStats(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/manager/dashboard/stats")
                .param("branchId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMembers").value(500L))
                .andExpect(jsonPath("$.monthlyRevenue").value(15000.0));
    }

    @Test
    void exportAnalyticsCsv_Success() throws Exception {
        String csvData = "Month,Revenue,NewJoins,Churn\nJan,1000,10,2";
        when(managerService.exportAnalyticsCsv()).thenReturn(csvData);

        mockMvc.perform(get("/api/v1/manager/dashboard/export-csv")
                .accept(MediaType.valueOf("text/csv")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=analytics_report.csv"))
                .andExpect(content().string(csvData));
    }

    @Test
    void getTransactionsDrillDown_Success() throws Exception {
        Invoice inv = new Invoice();
        inv.setInvoiceId(10L);
        
        when(invoiceRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(inv)));

        mockMvc.perform(get("/api/v1/manager/dashboard/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].invoiceId").value(10L));
    }
}
