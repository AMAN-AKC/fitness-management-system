package com.fitness.service;

import com.fitness.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadServiceTest {

    @InjectMocks
    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileUploadService, "uploadDirectory", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "maxFileSize", 5242880L);
        ReflectionTestUtils.setField(fileUploadService, "allowedTypes", "jpg,png,pdf");
    }

    @Test
    void uploadPhoto_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "dummy content".getBytes());
        String result = fileUploadService.uploadPhoto(file, 10L);
        assertTrue(result.startsWith("uploads/member_10_"));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    void uploadPhoto_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);
        assertThrows(BusinessRuleException.class, () -> fileUploadService.uploadPhoto(file, 10L));
    }

    @Test
    void uploadPhoto_TooLarge_ThrowsException() {
        // We set size directly with MockMultipartFile isn't easy, let's mock it
        MultipartFile file = new org.springframework.web.multipart.MultipartFile() {
            @Override
            public String getName() { return "file"; }
            @Override
            public String getOriginalFilename() { return "photo.jpg"; }
            @Override
            public String getContentType() { return "image/jpeg"; }
            @Override
            public boolean isEmpty() { return false; }
            @Override
            public long getSize() { return 10000000L; } // 10MB
            @Override
            public byte[] getBytes() { return new byte[0]; }
            @Override
            public java.io.InputStream getInputStream() { return null; }
            @Override
            public void transferTo(java.io.File dest) {}
        };
        
        assertThrows(BusinessRuleException.class, () -> fileUploadService.uploadPhoto(file, 10L));
    }

    @Test
    void uploadPhoto_InvalidType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "document.txt", "text/plain", "dummy content".getBytes());
        assertThrows(BusinessRuleException.class, () -> fileUploadService.uploadPhoto(file, 10L));
    }

    @Test
    void deletePhoto_Success() throws Exception {
        Path file = tempDir.resolve("test.jpg");
        Files.write(file, "content".getBytes());
        
        fileUploadService.deletePhoto(file.toString());
        
        assertFalse(Files.exists(file));
    }
}
