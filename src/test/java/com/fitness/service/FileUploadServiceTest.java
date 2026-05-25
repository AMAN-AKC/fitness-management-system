package com.fitness.service;

import com.fitness.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadServiceTest {

    @InjectMocks
    private FileUploadService fileUploadService;

    private String tempUploadDir;

    @BeforeEach
    void setUp() throws IOException {
        Path tempDir = Files.createTempDirectory("uploads");
        tempUploadDir = tempDir.toString();
        ReflectionTestUtils.setField(fileUploadService, "uploadDirectory", tempUploadDir);
        ReflectionTestUtils.setField(fileUploadService, "maxFileSize", 5242880L); // 5MB
        ReflectionTestUtils.setField(fileUploadService, "allowedTypes", "jpg,png,pdf");
    }

    @Test
    void uploadPhoto_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        String path = fileUploadService.uploadPhoto(file, 1L);

        assertNotNull(path);
        assertTrue(path.startsWith("uploads/member_1_"));
        assertTrue(path.endsWith(".jpg"));
        
        // cleanup
        try {
            Files.walk(Path.of(tempUploadDir))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ignored) {}
    }

    @Test
    void uploadPhoto_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertThrows(BusinessRuleException.class, () -> fileUploadService.uploadPhoto(file, 1L));
    }

    @Test
    void uploadPhoto_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        assertThrows(BusinessRuleException.class, () -> fileUploadService.uploadPhoto(file, 1L));
    }
    
    @Test
    void deletePhoto_Success() throws IOException {
        Path file = Files.createTempFile(Path.of(tempUploadDir), "test", ".jpg");
        assertTrue(Files.exists(file));
        
        fileUploadService.deletePhoto(file.toString());
        
        assertFalse(Files.exists(file));
    }
}
