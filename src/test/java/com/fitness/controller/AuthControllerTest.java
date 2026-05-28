package com.fitness.controller;

import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_Success() throws Exception {
        JwtResponse response = new JwtResponse("fake-jwt-token", "ADMIN", 1L, "testuser", "Test User", null);
        
        when(authService.login(any(LoginRequest.class), any(String.class))).thenReturn(response);

        String json = "{\"username\":\"testuser\", \"password\":\"password\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void forgotPassword_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password").param("email", "test@example.com"))
                .andExpect(status().isOk());
        
        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    void resetPassword_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .param("token", "123456")
                .param("newPassword", "NewPass1!"))
                .andExpect(status().isOk());
        
        verify(authService).resetPassword("123456", "NewPass1!");
    }
}
