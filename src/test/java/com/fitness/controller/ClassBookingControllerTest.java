package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.ClassBookingDTO;
import com.fitness.service.ClassBookingService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassBookingController.class)
public class ClassBookingControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;



    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClassBookingService classBookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MEMBER")
    void bookClass_Success() throws Exception {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        when(classBookingService.bookClass(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/bookings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classId").value(1L));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getBookingsByMember_Success() throws Exception {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);

        when(classBookingService.getBookingsByMember(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/bookings/member/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].classId").value(1L));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void cancelBooking_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/bookings/1/cancel")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(classBookingService).cancelBooking(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void overrideBooking_Success() throws Exception {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        when(classBookingService.overrideBooking(any(), eq(1L), eq("Emergency"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/bookings/override")
                .param("overrideByUserId", "1")
                .param("reason", "Emergency")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classId").value(1L));
    }
}
