package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.MemberDTO;
import com.fitness.service.MemberService;
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

@WebMvcTest(MemberController.class)
public class MemberControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMember_Success() throws Exception {
        MemberDTO dto = MemberDTO.builder()
                .memName("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .dob("1990-01-01")
                .homeBranchId(1L)
                .address("123 Main St")
                .emgContact("Jane Doe")
                .emgPhone("0987654321")
                .build();

        when(memberService.createMember(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllMembers_Success() throws Exception {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").build();
        when(memberService.getAllMembers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/members")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMemberById_Success() throws Exception {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").build();
        when(memberService.getMemberById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/members/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMember_Success() throws Exception {
        MemberDTO dto = MemberDTO.builder()
                .memName("John Doe")
                .email("updated@example.com")
                .phone("1234567890")
                .dob("1990-01-01")
                .homeBranchId(1L)
                .address("123 Main St")
                .emgContact("Jane Doe")
                .emgPhone("0987654321")
                .build();
        when(memberService.updateMember(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/members/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateMember_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(memberService).deactivateMember(1L);
    }
}
