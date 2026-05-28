package com.fitness.controller;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.service.PasswordPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PasswordPolicyControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPolicy_Success() throws Exception {
        PasswordPolicyDto dto = new PasswordPolicyDto();
        dto.setMinPasswordLength(8);

        when(passwordPolicyService.getPolicy()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/config/password-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minPasswordLength").value(8));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void updatePolicy_Success() throws Exception {
        PasswordPolicyDto dto = new PasswordPolicyDto();
        dto.setMinPasswordLength(10);

        when(passwordPolicyService.updatePolicy(any(PasswordPolicyDto.class), anyString())).thenReturn(dto);

        String json = "{\"minPasswordLength\":10}";

        mockMvc.perform(put("/api/v1/admin/config/password-policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minPasswordLength").value(10));
    }
}
