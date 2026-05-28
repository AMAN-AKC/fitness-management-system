package com.fitness.controller;

import com.fitness.dto.TrainerDTO;
import com.fitness.service.TrainerService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TrainerControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private TrainerService trainerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTrainer_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setTrainerId(100L);
        dto.setTrainerName("John Trainer");

        when(trainerService.createTrainer(any(TrainerDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1, \"branchId\":10, \"trainerName\":\"John Trainer\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainerName").value("John Trainer"));
    }

    @Test
    void getAllTrainers_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setTrainerId(100L);
        dto.setTrainerName("John Trainer");

        when(trainerService.getAllTrainers()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainerName").value("John Trainer"));
    }

    @Test
    void getTrainersByBranch_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setTrainerId(100L);
        when(trainerService.getTrainersByBranch(10L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/trainers/branch/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainerId").value(100));
    }

    @Test
    void getTrainerById_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setTrainerId(100L);
        when(trainerService.getTrainerById(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/trainers/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerId").value(100));
    }

    @Test
    void getTrainerByUserId_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setUserId(1L);
        when(trainerService.getTrainerByUserId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/trainers/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void updateTrainer_Success() throws Exception {
        TrainerDTO dto = new TrainerDTO();
        dto.setBio("Updated Bio");
        when(trainerService.updateTrainer(eq(100L), any(TrainerDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/trainers/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1, \"branchId\":10, \"trainerName\":\"John Trainer\", \"bio\":\"Updated Bio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated Bio"));
    }

    @Test
    void deactivateTrainer_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/trainers/100"))
                .andExpect(status().isNoContent());

        verify(trainerService).deactivateTrainer(100L);
    }
}
