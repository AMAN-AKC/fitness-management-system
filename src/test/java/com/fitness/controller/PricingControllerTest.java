package com.fitness.controller;

import com.fitness.dto.PriceBreakdownDTO;
import com.fitness.service.PriceBreakdownService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PricingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PricingControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PriceBreakdownService priceBreakdownService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPlanBreakdown_Success() throws Exception {
        PriceBreakdownDTO dto = PriceBreakdownDTO.builder()
                .planId(10L)
                .finalAmount(BigDecimal.valueOf(110))
                .build();

        when(priceBreakdownService.calculateNewPlanBreakdown(eq(10L), any(BigDecimal.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/pricing/plan/10/breakdown")
                .param("discountAmount", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(10))
                .andExpect(jsonPath("$.finalAmount").value(110));
    }

    @Test
    void getUpgradeBreakdown_Success() throws Exception {
        PriceBreakdownDTO dto = PriceBreakdownDTO.builder()
                .planId(20L)
                .finalAmount(BigDecimal.valueOf(150))
                .build();

        when(priceBreakdownService.calculateUpgradeBreakdown(eq(1L), eq(20L), any(BigDecimal.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/pricing/member/1/upgrade/20")
                .param("discountAmount", "20.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(20))
                .andExpect(jsonPath("$.finalAmount").value(150));
    }
}
