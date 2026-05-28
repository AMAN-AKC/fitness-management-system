package com.fitness.controller;

import com.fitness.dto.InvoiceDTO;
import com.fitness.service.InvoiceService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InvoiceControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private InvoiceService invoiceService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createInvoice_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(10L);
        dto.setMemberId(1L);

        when(invoiceService.createInvoice(any(InvoiceDTO.class))).thenReturn(dto);

        String json = "{\"memberId\":1, \"finalAmount\":100.0}";

        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").value(10));
    }

    @Test
    void getInvoicesByMember_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(10L);

        when(invoiceService.getInvoicesByMember(1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/invoices/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceId").value(10));
    }

    @Test
    void getInvoiceById_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(10L);

        when(invoiceService.getInvoiceById(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/invoices/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(10));
    }

    @Test
    void voidInvoice_Success() throws Exception {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(10L);

        when(invoiceService.voidInvoice(eq(10L), eq("Customer request"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/invoices/10/void")
                .param("reason", "Customer request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(10));
    }
}
