package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.HealthConsentDTO;
import com.fitness.dto.MemberDTO;
import com.fitness.entity.Member;
import com.fitness.service.HealthConsentService;
import com.fitness.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthConsentController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller logic tests
public class HealthConsentControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private HealthConsentService consentService;
    @MockBean
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitConsent_Success() throws Exception {
        HealthConsentDTO dto = HealthConsentDTO.builder()
                .memberId(1L)
                .medicalAcknowledged(true)
                .liabilityAcknowledged(true)
                .privacyAcknowledged(true)
                .parqResponses("responses")
                .formVersion("v1.0")
                .build();
                
        HealthConsentDTO responseDto = HealthConsentDTO.builder().consentId(100L).build();
        
        when(consentService.submitConsent(any(), anyString())).thenReturn(responseDto);
        
        mockMvc.perform(post("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consentId").value(100L));
    }

    @Test
    void getConsentsByMember_Success() throws Exception {
        HealthConsentDTO responseDto = HealthConsentDTO.builder().consentId(100L).build();
        when(consentService.getConsentsByMember(1L)).thenReturn(Arrays.asList(responseDto));
        
        mockMvc.perform(get("/api/v1/consents/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consentId").value(100L));
    }

    @Test
    void hasActiveConsent_Success() throws Exception {
        when(consentService.hasActiveConsent(1L)).thenReturn(true);
        
        mockMvc.perform(get("/api/v1/consents/member/1/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getConsentStatus_Success() throws Exception {
        when(consentService.getConsentStatus(1L)).thenReturn(Map.of("active", true, "memberId", 1L));
        
        mockMvc.perform(get("/api/v1/consents/member/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getCurrentMember_Success() throws Exception {
        Member mockMember = new Member();
        mockMember.setMemberId(10L);
        when(consentService.getCurrentMember()).thenReturn(mockMember);
        
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(10L);
        when(memberService.getMemberById(10L)).thenReturn(dto);
        
        mockMvc.perform(get("/api/v1/consents/me/member"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(10L));
    }

    @Test
    void addAdministrativeNote_Success() throws Exception {
        HealthConsentDTO responseDto = HealthConsentDTO.builder().consentId(100L).staffNotes("Noted").build();
        when(consentService.addAdministrativeNote(100L, "Noted")).thenReturn(responseDto);
        
        mockMvc.perform(patch("/api/v1/consents/100/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":\"Noted\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffNotes").value("Noted"));
    }

    @Test
    void downloadConsentHistory_Success() throws Exception {
        byte[] pdfBytes = "pdf-content".getBytes();
        when(consentService.downloadConsentHistoryPdf(1L)).thenReturn(pdfBytes);
        
        mockMvc.perform(get("/api/v1/consents/member/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"consent-history.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void anonymizedStats_Success() throws Exception {
        when(consentService.exportAnonymizedHealthStats()).thenReturn(Arrays.asList(Map.of("stat", "data")));
        
        mockMvc.perform(get("/api/v1/consents/stats/anonymized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stat").value("data"));
    }

    @Test
    void getPolicy_Success() throws Exception {
        when(consentService.getRetentionPolicy()).thenReturn(Map.of("policy", "val"));
        
        mockMvc.perform(get("/api/v1/consents/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy").value("val"));
    }

    @Test
    void purgeByRetentionPolicy_Success() throws Exception {
        when(consentService.deleteExpiredByRetentionPolicy()).thenReturn(5);
        
        mockMvc.perform(delete("/api/v1/consents/retention/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(5));
    }
}
