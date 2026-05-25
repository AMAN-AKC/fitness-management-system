package com.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.AuditLogDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.SystemUser;
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

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void log_Success() throws Exception {
        SystemUser user = new SystemUser();
        user.setUserId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        auditLogService.log(1L, "Member", 1L, AuditLog.Action.CREATE, "old", "new");

        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    void logForCurrentUser_Authenticated_Success() {
        SystemUser user = new SystemUser();
        user.setUserId(1L);

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        
        auditLogService.logForCurrentUser("Member", 1L, AuditLog.Action.UPDATE, "old", "new");
        
        verify(auditRepo).save(any(AuditLog.class));
    }

    @Test
    void getAllLogs_Success() {
        AuditLog log = new AuditLog();
        log.setAuditId(1L);
        log.setEntityName("Member");
        log.setEntityId(1L);

        when(auditRepo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(log));

        List<AuditLogDTO> result = auditLogService.getAllLogs();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAuditId());
    }
}
