package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.AttendanceDTO;
import com.fitness.service.AttendanceService;
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

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void checkIn_Success() throws Exception {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);
        dto.setScanMethod(com.fitness.entity.Attendance.ScanMethod.QR);

        when(attendanceService.checkIn(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/checkin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getAttendanceByMember_Success() throws Exception {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);

        when(attendanceService.getAttendanceByMember(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/attendance/member/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(1L));
    }

    @Test
    @WithMockUser(roles = "TRAINER")
    void markClassAttendance_Success() throws Exception {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);

        when(attendanceService.markClassAttendance(eq(1L), eq(1L), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/attendance/class/1/mark")
                .param("memberId", "1")
                .param("classId", "1")
                .param("branchId", "1")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L));
    }
}
