package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.entity.Member;
import com.fitness.repository.MemberRepository;
import com.fitness.service.AuditLogService;
import com.fitness.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoUploadController.class)
public class PhotoUploadControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadPhoto_Success() throws Exception {
        Member member = new Member();
        member.setMemberId(1L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(fileUploadService.uploadPhoto(any(), eq(1L))).thenReturn("uploads/member_1.jpg");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(multipart("/api/v1/members/1/photo")
                .file(file)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUploadPolicy_Success() throws Exception {
        when(fileUploadService.getUploadPolicy()).thenReturn("Policy details");

        mockMvc.perform(get("/api/v1/members/photo/policy")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy").value("Policy details"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePhoto_Success() throws Exception {
        Member member = new Member();
        member.setMemberId(1L);
        member.setPhotoPath("uploads/test.jpg");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        mockMvc.perform(delete("/api/v1/members/1/photo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fileUploadService).deletePhoto("uploads/test.jpg");
    }
}
