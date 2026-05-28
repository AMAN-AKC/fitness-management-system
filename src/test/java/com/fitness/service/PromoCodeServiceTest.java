package com.fitness.service;

import com.fitness.dto.PromoCodeDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Member;
import com.fitness.entity.PromoCode;
import com.fitness.entity.PromoCodeUsage;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.PromoCodeRepository;
import com.fitness.repository.PromoCodeUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromoCodeServiceTest {

    @InjectMocks
    private PromoCodeService promoCodeService;

    @Mock
    private PromoCodeRepository promoRepo;
    @Mock
    private PromoCodeUsageRepository promoUsageRepo;
    @Mock
    private MemberRepository memberRepo;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private PromoCode mockPromo;
    private PromoCodeDTO mockDto;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        mockPromo = new PromoCode();
        mockPromo.setPromoId(1L);
        mockPromo.setCode("SAVE20");
        mockPromo.setIsActive(true);
        mockPromo.setExpiryDate(LocalDate.now().plusDays(30));
        mockPromo.setUsageLimit(10);
        mockPromo.setPerMemberLimit(1);
        mockPromo.setEligibility(PromoCode.Eligibility.ALL);

        mockDto = new PromoCodeDTO();
        mockDto.setCode("SAVE20");

        mockMember = new Member();
        mockMember.setMemberId(10L);
        mockMember.setMemName("John Doe");
        mockMember.setStatus(Member.Status.ACTIVE);
    }

    @Test
    void createPromoCode_Success() {
        when(promoRepo.existsByCode("SAVE20")).thenReturn(false);
        when(mapper.map(mockDto, PromoCode.class)).thenReturn(mockPromo);
        when(promoRepo.save(mockPromo)).thenReturn(mockPromo);
        when(mapper.map(mockPromo, PromoCodeDTO.class)).thenReturn(mockDto);

        PromoCodeDTO result = promoCodeService.createPromoCode(mockDto);

        assertNotNull(result);
        assertEquals("SAVE20", result.getCode());
        verify(auditLogService).logForCurrentUser(eq("PromoCode"), eq(1L), eq(AuditLog.Action.CREATE), anyString(), isNull());
    }

    @Test
    void createPromoCode_DuplicateCode_ThrowsException() {
        when(promoRepo.existsByCode("SAVE20")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> promoCodeService.createPromoCode(mockDto));
    }

    @Test
    void validateAndGet_Success() {
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        when(promoUsageRepo.countByPromoCodePromoId(1L)).thenReturn(5L);
        when(promoUsageRepo.countByPromoCodePromoIdAndMemberMemberId(1L, 10L)).thenReturn(0L);
        when(mapper.map(mockPromo, PromoCodeDTO.class)).thenReturn(mockDto);

        PromoCodeDTO result = promoCodeService.validateAndGet("SAVE20", 10L);
        assertNotNull(result);
    }

    @Test
    void validateAndGet_Inactive_ThrowsException() {
        mockPromo.setIsActive(false);
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        assertThrows(BusinessRuleException.class, () -> promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void validateAndGet_Expired_ThrowsException() {
        mockPromo.setExpiryDate(LocalDate.now().minusDays(1));
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        assertThrows(BusinessRuleException.class, () -> promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void validateAndGet_GlobalLimitExceeded_ThrowsException() {
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        when(promoUsageRepo.countByPromoCodePromoId(1L)).thenReturn(10L);
        assertThrows(BusinessRuleException.class, () -> promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void validateAndGet_PerMemberLimitExceeded_ThrowsException() {
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        when(promoUsageRepo.countByPromoCodePromoId(1L)).thenReturn(5L);
        when(promoUsageRepo.countByPromoCodePromoIdAndMemberMemberId(1L, 10L)).thenReturn(1L);
        assertThrows(BusinessRuleException.class, () -> promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void validateAndGet_NewMemberOnly_FailsForActiveMember() {
        mockPromo.setEligibility(PromoCode.Eligibility.NEW);
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        when(promoUsageRepo.countByPromoCodePromoId(1L)).thenReturn(5L);
        when(promoUsageRepo.countByPromoCodePromoIdAndMemberMemberId(1L, 10L)).thenReturn(0L);
        when(memberRepo.findById(10L)).thenReturn(Optional.of(mockMember));
        
        assertThrows(BusinessRuleException.class, () -> promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void validateAndGet_NewMemberOnly_SucceedsForProspect() {
        mockPromo.setEligibility(PromoCode.Eligibility.NEW);
        mockMember.setStatus(Member.Status.PROSPECT);
        when(promoRepo.findByCode("SAVE20")).thenReturn(Optional.of(mockPromo));
        when(promoUsageRepo.countByPromoCodePromoId(1L)).thenReturn(5L);
        when(promoUsageRepo.countByPromoCodePromoIdAndMemberMemberId(1L, 10L)).thenReturn(0L);
        when(memberRepo.findById(10L)).thenReturn(Optional.of(mockMember));
        when(mapper.map(mockPromo, PromoCodeDTO.class)).thenReturn(mockDto);
        
        assertNotNull(promoCodeService.validateAndGet("SAVE20", 10L));
    }

    @Test
    void getAllPromoCodes_Success() {
        when(promoRepo.findAll()).thenReturn(Collections.singletonList(mockPromo));
        when(mapper.map(mockPromo, PromoCodeDTO.class)).thenReturn(mockDto);
        
        List<PromoCodeDTO> results = promoCodeService.getAllPromoCodes();
        assertEquals(1, results.size());
    }

    @Test
    void deactivatePromoCode_Success() {
        when(promoRepo.findById(1L)).thenReturn(Optional.of(mockPromo));
        
        promoCodeService.deactivatePromoCode(1L);
        
        assertFalse(mockPromo.getIsActive());
        verify(promoRepo).save(mockPromo);
        verify(auditLogService).logForCurrentUser(eq("PromoCode"), eq(1L), eq(AuditLog.Action.UPDATE), anyString(), anyString());
    }

    @Test
    void exportPromoUsageCsv_Success() {
        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setId(100L);
        usage.setPromoCode(mockPromo);
        usage.setMember(mockMember);
        usage.setUsedAt(LocalDateTime.now());
        
        when(promoUsageRepo.findAll()).thenReturn(Collections.singletonList(usage));
        
        byte[] csv = promoCodeService.exportPromoUsageCsv();
        
        assertNotNull(csv);
        String csvString = new String(csv, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvString.contains("UsageID,PromoCode,MemberID,MemberName"));
        assertTrue(csvString.contains("100,SAVE20,10,John Doe"));
    }
}
