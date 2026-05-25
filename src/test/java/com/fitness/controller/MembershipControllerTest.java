package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.MembershipDTO;
import com.fitness.service.MembershipService;
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

@WebMvcTest(MembershipController.class)
public class MembershipControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipService membershipService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMembership_Success() throws Exception {
        MembershipDTO dto = new MembershipDTO();
        dto.setMemberId(1L);
        dto.setPlanId(1L);

        when(membershipService.createMembership(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/memberships")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithMockUser
    void getMembershipById_Success() throws Exception {
        MembershipDTO dto = new MembershipDTO();
        dto.setMemberId(1L);

        when(membershipService.getMembershipById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/memberships/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithMockUser
    void getMembershipsByMember_Success() throws Exception {
        MembershipDTO dto = new MembershipDTO();
        dto.setMemberId(1L);

        when(membershipService.getMembershipsByMember(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/memberships/member/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(1L));
    }
}
