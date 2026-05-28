package com.fitness.controller;

import com.fitness.dto.InvoiceDTO;
import com.fitness.dto.RecurringBillingScheduleDTO;
import com.fitness.entity.Membership;
import com.fitness.service.RecurringBillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecurringBillingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RecurringBillingControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private RecurringBillingService recurringBillingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createSchedule_Success() throws Exception {
        Membership m = new Membership();
        m.setMemId(10L);
        when(recurringBillingService.getMembershipById(10L)).thenReturn(m);

        RecurringBillingScheduleDTO dto = RecurringBillingScheduleDTO.builder().membershipId(10L).build();
        when(recurringBillingService.createRecurringSchedule(m)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/recurring-billing/membership/10/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipId").value(10L));
    }

    @Test
    void getMembershipsForBilling_Success() throws Exception {
        Membership m = new Membership();
        m.setMemId(10L);
        when(recurringBillingService.getMembershipsForRecurringBilling(LocalDate.parse("2030-01-01")))
                .thenReturn(Collections.singletonList(m));

        mockMvc.perform(get("/api/v1/recurring-billing/billing-due/2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void runRecurringBillingNow_Success() throws Exception {
        InvoiceDTO invoice = new InvoiceDTO();
        invoice.setInvoiceNumber("INV-123");
        when(recurringBillingService.processRecurringBilling(any())).thenReturn(Collections.singletonList(invoice));

        mockMvc.perform(post("/api/v1/recurring-billing/run-today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.createdInvoices").value(1));
    }

    @Test
    void pauseRecurringBilling_Success() throws Exception {
        mockMvc.perform(post("/api/v1/recurring-billing/10/pause")
                .param("reason", "vacation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
                
        verify(recurringBillingService).pauseRecurringBilling(10L, "vacation");
    }

    @Test
    void resumeRecurringBilling_Success() throws Exception {
        mockMvc.perform(post("/api/v1/recurring-billing/10/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
                
        verify(recurringBillingService).resumeRecurringBilling(10L);
    }
}
