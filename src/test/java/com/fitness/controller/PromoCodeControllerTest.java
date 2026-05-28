package com.fitness.controller;

import com.fitness.dto.PromoCodeDTO;
import com.fitness.service.PromoCodeService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromoCodeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PromoCodeControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PromoCodeService promoCodeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPromoCode_Success() throws Exception {
        PromoCodeDTO dto = new PromoCodeDTO();
        dto.setCode("SAVE20");
        
        when(promoCodeService.createPromoCode(any(PromoCodeDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/promo-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"SAVE20\", \"discountType\":\"PERCENT\", \"discountValue\":20.0, \"expiryDate\":\"2030-01-01\", \"usageLimit\":10, \"perMemberLimit\":1, \"eligibility\":\"ALL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SAVE20"));
    }

    @Test
    void validateCode_Success() throws Exception {
        PromoCodeDTO dto = new PromoCodeDTO();
        dto.setCode("SAVE20");
        when(promoCodeService.validateAndGet("SAVE20", 1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/promo-codes/validate/SAVE20")
                .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE20"));
    }

    @Test
    void getAllPromoCodes_Success() throws Exception {
        PromoCodeDTO dto = new PromoCodeDTO();
        dto.setCode("SAVE20");
        when(promoCodeService.getAllPromoCodes()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/promo-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SAVE20"));
    }

    @Test
    void deactivatePromoCode_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/promo-codes/1"))
                .andExpect(status().isNoContent());
                
        verify(promoCodeService).deactivatePromoCode(1L);
    }

    @Test
    void exportPromoUsageCsv_Success() throws Exception {
        byte[] csv = "UsageID,PromoCode\n1,SAVE20".getBytes();
        when(promoCodeService.exportPromoUsageCsv()).thenReturn(csv);

        mockMvc.perform(get("/api/v1/promo-codes/export-usage"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=promo_usage.csv"))
                .andExpect(content().bytes(csv));
    }
}
