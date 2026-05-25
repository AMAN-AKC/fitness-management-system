package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
)
public class AuthControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void login_Success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("pass");

        JwtResponse res = new JwtResponse("token", "ADMIN", 1L, "admin", "Admin", 1L);

        when(authService.login(any(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void forgotPassword_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .param("email", "admin@test.com")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void resetPassword_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .param("token", "123456")
                .param("newPassword", "Pass123!")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
