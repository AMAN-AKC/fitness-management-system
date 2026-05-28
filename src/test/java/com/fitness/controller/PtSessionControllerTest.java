package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.PtSessionDTO;
import com.fitness.entity.PtSession;
import com.fitness.service.PtSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PtSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PtSessionControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PtSessionService ptService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requestSession_Success() throws Exception {
        PtSessionDTO requestDto = new PtSessionDTO();
        requestDto.setMemberId(1L);
        requestDto.setTrainerId(2L);
        requestDto.setDurationMins(60);
        requestDto.setScheduledAt("2030-01-01T10:00:00");
        
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        
        when(ptService.requestSession(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/pt-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(10L));
    }

    @Test
    void updateStatus_Success() throws Exception {
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        responseDto.setStatus(PtSession.Status.APPROVED);

        when(ptService.updateStatus(eq(10L), eq(PtSession.Status.APPROVED), eq("notes"))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/pt-sessions/10/status")
                .param("status", "APPROVED")
                .param("notes", "notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rescheduleSession_Success() throws Exception {
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        
        when(ptService.rescheduleSession(eq(10L), eq("2030-01-01T10:00:00"))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/pt-sessions/10/reschedule")
                .param("newScheduledAt", "2030-01-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(10L));
    }

    @Test
    void cancelSession_Success() throws Exception {
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        
        when(ptService.cancelSession(10L)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/pt-sessions/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(10L));
    }

    @Test
    void getSessionsByMember_Success() throws Exception {
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        
        when(ptService.getSessionsByMember(1L)).thenReturn(Arrays.asList(responseDto));

        mockMvc.perform(get("/api/v1/pt-sessions/member/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(10L));
    }

    @Test
    void getSessionsByTrainer_Success() throws Exception {
        PtSessionDTO responseDto = new PtSessionDTO();
        responseDto.setSessionId(10L);
        
        when(ptService.getSessionsByTrainer(2L)).thenReturn(Arrays.asList(responseDto));

        mockMvc.perform(get("/api/v1/pt-sessions/trainer/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(10L));
    }
}
