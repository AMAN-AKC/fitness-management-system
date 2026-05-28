package com.fitness.controller;

import com.fitness.dto.BranchDTO;
import com.fitness.service.BranchService;
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

@WebMvcTest(BranchController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BranchControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private BranchService branchService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBranch_Success() throws Exception {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(1L);
        dto.setBranchName("Downtown Branch");

        when(branchService.createBranch(any(BranchDTO.class))).thenReturn(dto);

        String json = "{\"branchName\":\"Downtown Branch\", \"address\":\"123 Main St\", \"contact\":\"555-1234\", \"opHours\":\"9-5\", \"timezone\":\"EST\"}";

        mockMvc.perform(post("/api/v1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.branchName").value("Downtown Branch"));
    }

    @Test
    void getActiveBranches_Success() throws Exception {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(1L);
        dto.setBranchName("Downtown Branch");

        when(branchService.getActiveBranches()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchName").value("Downtown Branch"));
    }

    @Test
    void getBranchById_Success() throws Exception {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(1L);
        when(branchService.getBranchById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/branches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(1));
    }

    @Test
    void updateBranch_Success() throws Exception {
        BranchDTO dto = new BranchDTO();
        dto.setBranchName("Updated Branch");
        when(branchService.updateBranch(eq(1L), any(BranchDTO.class))).thenReturn(dto);

        String json = "{\"branchName\":\"Updated Branch\", \"address\":\"123 Main St\", \"contact\":\"555-1234\", \"opHours\":\"9-5\", \"timezone\":\"EST\"}";

        mockMvc.perform(put("/api/v1/branches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchName").value("Updated Branch"));
    }

    @Test
    void deactivateBranch_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/branches/1"))
                .andExpect(status().isNoContent());

        verify(branchService).deactivateBranch(1L);
    }
}
