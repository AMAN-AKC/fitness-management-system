package com.fitness.controller;

import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.service.DunningService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DunningController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DunningControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private DunningService dunningService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOverdueInvoices_Success() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(10L);
        when(dunningService.getOverdueInvoices()).thenReturn(Collections.singletonList(invoice));

        mockMvc.perform(get("/api/v1/dunning/overdue-invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void getInvoicesOverdueByDays_Success() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(10L);
        when(dunningService.getInvoicesOverdueByDays(5)).thenReturn(Collections.singletonList(invoice));

        mockMvc.perform(get("/api/v1/dunning/overdue-by-days").param("days", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void getDunningMemberships_Success() throws Exception {
        Membership membership = new Membership();
        membership.setMemId(20L);
        when(dunningService.getDunningMemberships()).thenReturn(Collections.singletonList(membership));

        mockMvc.perform(get("/api/v1/dunning/dunning-memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void resolveDunning_Success() throws Exception {
        mockMvc.perform(post("/api/v1/dunning/resolve/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        
        verify(dunningService).resolveDunning(20L);
    }

    @Test
    void suspendDunningMembership_Success() throws Exception {
        mockMvc.perform(post("/api/v1/dunning/suspend/20").param("reason", "Extended non-payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        
        verify(dunningService).suspendDunningMembership(20L, "Extended non-payment");
    }

    @Test
    void recordFollowUp_Success() throws Exception {
        mockMvc.perform(post("/api/v1/dunning/follow-up/10").param("notes", "Called member"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        
        verify(dunningService).recordFollowUp(10L, "Called member");
    }

    @Test
    void setPromiseToPay_Success() throws Exception {
        mockMvc.perform(post("/api/v1/dunning/promise-to-pay/10").param("promiseDate", "2024-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        
        verify(dunningService).setPromiseToPay(10L, LocalDate.parse("2024-12-01"));
    }

    @Test
    void exportDunningListCsv_Success() throws Exception {
        Member member = new Member();
        member.setMemName("Jane Doe");
        member.setEmail("jane@example.com");
        member.setPhone("1234567890");

        Plan plan = new Plan();
        plan.setPlanName("Gold Plan");

        Membership membership = new Membership();
        membership.setMemId(20L);
        membership.setMember(member);
        membership.setPlan(plan);
        membership.setStatus(Membership.Status.DUNNING);

        when(dunningService.getDunningMemberships()).thenReturn(Collections.singletonList(membership));

        mockMvc.perform(get("/api/v1/dunning/export-csv").accept("text/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=dunning_list.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Jane Doe")));
    }
}
