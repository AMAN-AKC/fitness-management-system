package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.SystemUserDTO;
import com.fitness.service.SystemUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SystemUserControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private SystemUserService userService;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_Success() throws Exception {
        SystemUserDTO requestDto = new SystemUserDTO();
        requestDto.setUsername("newuser");
        requestDto.setEmail("new@example.com");

        SystemUserDTO responseDto = new SystemUserDTO();
        responseDto.setUsername("newuser");
        responseDto.setBranchName("HQ");

        when(userService.createUser(any(), eq("securePass"))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users")
                .param("password", "securePass")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.branchName").value("HQ"));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        SystemUserDTO dto = new SystemUserDTO();
        dto.setUsername("user1");
        
        when(userService.getAllUsers()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void getUserById_Success() throws Exception {
        SystemUserDTO dto = new SystemUserDTO();
        dto.setUsername("user1");
        
        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void updateUser_Success() throws Exception {
        SystemUserDTO requestDto = new SystemUserDTO();
        requestDto.setUsername("updated");
        requestDto.setEmail("update@example.com");
        
        SystemUserDTO responseDto = new SystemUserDTO();
        responseDto.setUsername("updated");
        
        when(userService.updateUser(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void deactivateUser_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
                
        verify(userService).deactivateUser(1L);
    }

    @Test
    void lockUser_Success() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/lock"))
                .andExpect(status().isNoContent());
                
        verify(userService).lockUser(1L);
    }

    @Test
    void unlockUser_Success() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/unlock"))
                .andExpect(status().isNoContent());
                
        verify(userService).unlockUser(1L);
    }
}
