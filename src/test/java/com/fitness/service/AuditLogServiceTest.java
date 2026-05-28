package com.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.AuditLogDTO;
import com.fitness.entity.AuditLog;
import com.fitness.enums.Role;
import com.fitness.entity.SystemUser;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.AuditLogRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @InjectMocks
    private AuditLogService auditLogService;

    @Mock
    private AuditLogRepository auditRepo;
    @Mock
    private SystemUserRepository userRepo;
    @Mock
    private ModelMapper mapper;
    @Mock
    private ObjectMapper objectMapper;

    private SystemUser mockUser;
    private AuditLog mockLog;

    @BeforeEach
    void setUp() {
        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setRole(Role.ADMIN);

        mockLog = new AuditLog();
        mockLog.setAuditId(100L);
        mockLog.setPerformedBy(mockUser);
        mockLog.setEntityName("TestEntity");
        mockLog.setEntityId(10L);
        mockLog.setAction(AuditLog.Action.CREATE);
        mockLog.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void log_Success() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Not json"));
        when(objectMapper.writeValueAsString(anyString())).thenReturn("\"test\"");

        auditLogService.log(1L, "TestEntity", 10L, AuditLog.Action.CREATE, "old", "new");

        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    void log_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> 
            auditLogService.log(1L, "TestEntity", 10L, AuditLog.Action.CREATE, "old", "new"));
    }

    @Test
    void getAllLogs_Success() {
        when(auditRepo.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(mockLog));
        List<AuditLogDTO> results = auditLogService.getAllLogs();
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getAuditId());
    }

    @Test
    void getLogsByUser_Success() {
        when(auditRepo.findByPerformedByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.singletonList(mockLog));
        List<AuditLogDTO> results = auditLogService.getLogsByUser(1L);
        assertEquals(1, results.size());
    }

    @Test
    void getLogsByEntity_Success() {
        when(auditRepo.findByEntityNameAndEntityId("TestEntity", 10L)).thenReturn(Collections.singletonList(mockLog));
        List<AuditLogDTO> results = auditLogService.getLogsByEntity("TestEntity", 10L);
        assertEquals(1, results.size());
    }

    @Test
    void logForSystem_Success() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        auditLogService.logForSystem("TestEntity", 10L, AuditLog.Action.CREATE, null, null);
        
        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    void logForCurrentUser_WithAuth_Success() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "pass")
        );
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

        auditLogService.logForCurrentUser("TestEntity", 10L, AuditLog.Action.UPDATE, null, null);

        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    void logForCurrentUser_NoAuth_FallbackToSystem() throws Exception {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

        auditLogService.logForCurrentUser("TestEntity", 10L, AuditLog.Action.UPDATE, null, null);

        verify(auditRepo).save(any(AuditLog.class));
    }
}
