package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.InvoiceDTO;
import com.fitness.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
public class InvoiceControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInvoice_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setMemberId(1L);

        when(invoiceService.createInvoice(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/invoices")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void voidInvoice_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setMemberId(1L);

        when(invoiceService.voidInvoice(eq(1L), eq("Mistake"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/invoices/1/void")
                .param("reason", "Mistake")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getInvoicesByMember_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setMemberId(1L);

        when(invoiceService.getInvoicesByMember(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/invoices/member/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(1L));
    }
}
