package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.ClassesDTO;
import com.fitness.service.ClassesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassesController.class)
public class ClassesControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClassesService classesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClass_Success() throws Exception {
        ClassesDTO dto = new ClassesDTO();
        dto.setClassName("Yoga");
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(1L);
        dto.setStartDate("2026-06-01");
        dto.setEndDate("2026-06-30");
        dto.setWeekdays("Mon,Wed");
        dto.setClassTime("10:00");
        dto.setDurationMins(60);
        dto.setCapacity(20);

        when(classesService.createClass(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/classes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.className").value("Yoga"));
    }

    @Test
    @WithMockUser
    void getAllClasses_Success() throws Exception {
        ClassesDTO dto = new ClassesDTO();
        dto.setClassName("Yoga");

        when(classesService.getAllClasses()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/classes")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].className").value("Yoga"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelClass_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/classes/1/cancel")
                .param("reason", "Trainer unavailable")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(classesService).cancelClass(1L, "Trainer unavailable");
    }
}
