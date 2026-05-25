package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.PaymentDTO;
import com.fitness.service.PaymentService;
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

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MEMBER")
    void processPayment_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);
        dto.setMemberId(1L);
        dto.setPaymentMethod(com.fitness.entity.Payment.PaymentMethod.CARD);
        dto.setAmountPaid(new java.math.BigDecimal("100.00"));

        when(paymentService.processPayment(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void refundPayment_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);

        when(paymentService.refundPayment(eq(1L), eq(1L), eq("Mistake"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/payments/1/refund")
                .param("refundBy", "1")
                .param("reason", "Mistake")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(1L));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getPaymentsByMember_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);

        when(paymentService.getPaymentsByMember(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/payments/member/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceId").value(1L));
    }
}
