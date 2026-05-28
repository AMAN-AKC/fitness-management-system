package com.fitness.controller;

import com.fitness.dto.PaymentDTO;
import com.fitness.service.PaymentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void processPayment_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(100L);

        when(paymentService.processPayment(any(PaymentDTO.class))).thenReturn(dto);

        String json = "{\"invoiceId\":10, \"memberId\":1, \"paymentMethod\":\"CARD\", \"amountPaid\":100.0}";

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(100));
    }

    @Test
    void refundPayment_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(100L);

        when(paymentService.refundPayment(eq(100L), eq(2L), eq("Customer request"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/payments/100/refund")
                .param("refundBy", "2")
                .param("reason", "Customer request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(100));
    }

    @Test
    void getPaymentsByMember_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(100L);

        when(paymentService.getPaymentsByMember(1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/payments/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(100));
    }

    @Test
    void getFailedPayments_Success() throws Exception {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(100L);

        when(paymentService.getFailedPayments()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/payments/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(100));
    }

    @Test
    void getRevenueMTD_Success() throws Exception {
        when(paymentService.getRevenueMTD()).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/v1/payments/revenue/mtd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000));
    }
}
