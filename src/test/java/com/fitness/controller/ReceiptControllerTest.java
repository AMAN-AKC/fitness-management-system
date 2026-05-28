package com.fitness.controller;

import com.fitness.dto.ReceiptDTO;
import com.fitness.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReceiptControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private ReceiptService receiptService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMemberReceipts_Success() throws Exception {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setReceiptNumber("RCP-1234");

        when(receiptService.getReceiptsByMember(1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/receipts/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptNumber").value("RCP-1234"));
    }

    @Test
    void getReceiptByNumber_Success() throws Exception {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setReceiptNumber("RCP-1234");

        when(receiptService.getReceiptByNumber("RCP-1234")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/receipts/RCP-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("RCP-1234"));
    }

    @Test
    void markAsEmailed_Success() throws Exception {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setReceiptNumber("RCP-1234");

        when(receiptService.markAsEmailed(10L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/receipts/10/email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").value("RCP-1234"));
    }
}
