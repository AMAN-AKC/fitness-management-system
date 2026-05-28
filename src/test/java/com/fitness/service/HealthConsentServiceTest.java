package com.fitness.service;

import com.fitness.dto.HealthConsentDTO;
import com.fitness.entity.*;
import com.fitness.enums.Role;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthConsentServiceTest {

    @InjectMocks
    private HealthConsentService healthConsentService;

    @Mock
    private HealthConsentRepository consentRepo;
    
    @Mock
    private MemberRepository memberRepo;
    
    @Mock
    private SystemUserRepository userRepo;
    
    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private SystemUser mockUser;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(healthConsentService, "currentVersion", "v1.0");
        ReflectionTestUtils.setField(healthConsentService, "consentValidityDays", 365);
        ReflectionTestUtils.setField(healthConsentService, "retentionDays", 2555);

        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setRole(Role.MEMBER);

        mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setStatus(Member.Status.ACTIVE);
        mockMember.setUser(mockUser);
    }

    private void mockSecurityContext(SystemUser user) {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(user.getUsername());
        lenient().when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        lenient().when(memberRepo.findByUserUserId(user.getUserId())).thenReturn(Optional.of(mockMember));
    }
    
    private void mockNoSecurityContext() {
        SecurityContextHolder.clearContext();
        lenient().when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        lenient().when(memberRepo.findByUserUserId(1L)).thenReturn(Optional.of(mockMember));
    }

    @Test
    void submitConsent_Success_ActivatesProspect() {
        mockNoSecurityContext();
        
        HealthConsentDTO dto = HealthConsentDTO.builder()
                .memberId(1L)
                .medicalAcknowledged(true)
                .liabilityAcknowledged(true)
                .privacyAcknowledged(true)
                .parqResponses("none")
                .build();

        mockMember.setStatus(Member.Status.PROSPECT);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(consentRepo.findByMemberMemberIdAndStatus(1L, HealthConsent.Status.ACTIVE)).thenReturn(Optional.empty());

        HealthConsent savedConsent = HealthConsent.builder()
                .consentId(10L)
                .member(mockMember)
                .formVersion("v1.0")
                .status(HealthConsent.Status.ACTIVE)
                .build();
        
        when(consentRepo.save(any(HealthConsent.class))).thenReturn(savedConsent);
        
        Membership membership = new Membership();
        membership.setStartDate(java.time.LocalDate.now());
        when(membershipRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(membership));

        HealthConsentDTO result = healthConsentService.submitConsent(dto, "127.0.0.1");

        assertNotNull(result);
        assertEquals(10L, result.getConsentId());
        assertEquals(Member.Status.ACTIVE, mockMember.getStatus());
        verify(memberRepo).save(mockMember);
        verify(auditLogService, atLeastOnce()).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void submitConsent_MissingAcknowledgments_ThrowsException() {
        mockNoSecurityContext();
        
        HealthConsentDTO dto = HealthConsentDTO.builder()
                .memberId(1L)
                .medicalAcknowledged(true)
                .liabilityAcknowledged(false)
                .build();

        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));

        assertThrows(BusinessRuleException.class, () -> healthConsentService.submitConsent(dto, "127.0.0.1"));
    }

    @Test
    void getConsentsByMember_AsMember_Success() {
        mockSecurityContext(mockUser); // Role MEMBER

        HealthConsent consent = HealthConsent.builder()
                .consentId(1L)
                .member(mockMember)
                .formVersion("v1.0")
                .parqResponses("Sensitive data")
                .ipAddress("192.168.1.1")
                .status(HealthConsent.Status.ACTIVE)
                .build();

        when(consentRepo.findByMemberMemberIdOrderByAcknowledgedAtDesc(1L)).thenReturn(Collections.singletonList(consent));

        List<HealthConsentDTO> results = healthConsentService.getConsentsByMember(1L);

        assertEquals(1, results.size());
        assertEquals("Sensitive data", results.get(0).getParqResponses()); // Not masked for the member themselves
    }
    
    @Test
    void getConsentsByMember_AsOtherMember_ThrowsException() {
        mockSecurityContext(mockUser); // Role MEMBER, userId 1, memberId 1

        assertThrows(BusinessRuleException.class, () -> healthConsentService.getConsentsByMember(2L));
    }

    @Test
    void getConsentsByMember_AsTrainer_MasksSensitiveData() {
        SystemUser trainer = new SystemUser();
        trainer.setUserId(2L);
        trainer.setUsername("trainer");
        trainer.setRole(Role.TRAINER);
        mockSecurityContext(trainer);

        HealthConsent consent = HealthConsent.builder()
                .consentId(1L)
                .member(mockMember)
                .formVersion("v1.0")
                .parqResponses("Sensitive data")
                .ipAddress("192.168.1.1")
                .status(HealthConsent.Status.ACTIVE)
                .build();

        when(consentRepo.findByMemberMemberIdOrderByAcknowledgedAtDesc(1L)).thenReturn(Collections.singletonList(consent));

        List<HealthConsentDTO> results = healthConsentService.getConsentsByMember(1L);

        assertEquals(1, results.size());
        assertEquals("****", results.get(0).getParqResponses());
        assertEquals("****", results.get(0).getIpAddress());
    }

    @Test
    void hasActiveConsent_ReturnsTrue() {
        mockNoSecurityContext();
        HealthConsent consent = HealthConsent.builder()
                .status(HealthConsent.Status.ACTIVE)
                .formVersion("v1.0")
                .expiresAt(LocalDateTime.now().plusDays(10))
                .member(mockMember)
                .build();
                
        when(consentRepo.findTopByMemberMemberIdOrderByAcknowledgedAtDesc(1L)).thenReturn(Optional.of(consent));
        
        assertTrue(healthConsentService.hasActiveConsent(1L));
    }

    @Test
    void hasActiveConsent_Expired_ReturnsFalse() {
        mockNoSecurityContext();
        HealthConsent consent = HealthConsent.builder()
                .status(HealthConsent.Status.ACTIVE)
                .formVersion("v1.0")
                .expiresAt(LocalDateTime.now().minusDays(10))
                .member(mockMember)
                .build();
                
        when(consentRepo.findTopByMemberMemberIdOrderByAcknowledgedAtDesc(1L)).thenReturn(Optional.of(consent));
        
        assertFalse(healthConsentService.hasActiveConsent(1L));
    }

    @Test
    void addAdministrativeNote_Success() {
        HealthConsent consent = HealthConsent.builder()
                .consentId(1L)
                .member(mockMember)
                .staffNotes("Old Note")
                .formVersion("v1.0")
                .build();
                
        when(consentRepo.findById(1L)).thenReturn(Optional.of(consent));
        when(consentRepo.save(any())).thenReturn(consent);
        
        HealthConsentDTO result = healthConsentService.addAdministrativeNote(1L, "New Note");
        
        assertEquals("New Note", result.getStaffNotes());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void addAdministrativeNote_DiagnosticNote_ThrowsException() {
        assertThrows(BusinessRuleException.class, () -> healthConsentService.addAdministrativeNote(1L, "Patient needs diagnosis"));
    }

    @Test
    void downloadConsentHistoryPdf_Success() {
        mockNoSecurityContext();
        HealthConsent consent = HealthConsent.builder()
                .consentId(1L)
                .member(mockMember)
                .formVersion("v1.0")
                .status(HealthConsent.Status.ACTIVE)
                .build();
                
        when(consentRepo.findByMemberMemberIdOrderByAcknowledgedAtDesc(1L)).thenReturn(Collections.singletonList(consent));
        
        byte[] pdf = healthConsentService.downloadConsentHistoryPdf(1L);
        
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
    
    @Test
    void deleteExpiredByRetentionPolicy_Success() {
        HealthConsent consent1 = new HealthConsent();
        consent1.setConsentId(1L);
        HealthConsent consent2 = new HealthConsent();
        consent2.setConsentId(2L);
        
        when(consentRepo.findByAcknowledgedAtBefore(any(LocalDateTime.class))).thenReturn(Arrays.asList(consent1, consent2));
        
        int deleted = healthConsentService.deleteExpiredByRetentionPolicy();
        
        assertEquals(2, deleted);
        verify(consentRepo).deleteAll(anyList());
        verify(auditLogService, times(2)).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }
    
    @Test
    void exportAnonymizedHealthStats_Success() {
        Object[] row = new Object[]{"v1.0", HealthConsent.Status.ACTIVE, 10L, 2L};
        when(consentRepo.summarizeAnonymizedStats()).thenReturn(Collections.singletonList(row));
        
        List<Map<String, Object>> stats = healthConsentService.exportAnonymizedHealthStats();
        
        assertEquals(1, stats.size());
        assertEquals("v1.0", stats.get(0).get("formVersion"));
        assertEquals(10L, stats.get(0).get("consentCount"));
    }
}
