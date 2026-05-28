package com.fitness.controller;

import com.fitness.entity.Member;
import com.fitness.repository.MemberRepository;
import com.fitness.service.AuditLogService;
import com.fitness.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhotoUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PhotoUploadControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private FileUploadService fileUploadService;
    
    @MockBean
    private MemberRepository memberRepository;
    
    @MockBean
    private AuditLogService auditLogService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void uploadPhoto_Success() throws Exception {
        Member member = new Member();
        member.setMemberId(10L);
        member.setPhotoPath("old/path.jpg");

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));
        when(fileUploadService.uploadPhoto(any(), eq(10L))).thenReturn("uploads/member_10_xyz.jpg");

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/members/10/photo")
                .file(file)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filePath").value("uploads/member_10_xyz.jpg"));
        
        verify(memberRepository).save(member);
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void uploadPhoto_Failure() throws Exception {
        Member member = new Member();
        member.setMemberId(10L);

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));
        when(fileUploadService.uploadPhoto(any(), eq(10L))).thenThrow(new RuntimeException("Upload failed"));

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/members/10/photo")
                .file(file)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void getUploadPolicy_Success() throws Exception {
        when(fileUploadService.getUploadPolicy()).thenReturn("Max 5MB");

        mockMvc.perform(get("/api/v1/members/photo/policy")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policy").value("Max 5MB"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePhoto_Success() throws Exception {
        Member member = new Member();
        member.setMemberId(10L);
        member.setPhotoPath("old/path.jpg");

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        mockMvc.perform(delete("/api/v1/members/10/photo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(fileUploadService).deletePhoto("old/path.jpg");
        verify(memberRepository).save(member);
    }
}
