package com.fitness.service;

import com.fitness.dto.BulkImportReport;
import com.fitness.dto.BulkImportRowResult;
import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvImportServiceTest {

    @InjectMocks
    private CsvImportService csvImportService;

    @Mock
    private MemberService memberService;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SystemUserRepository systemUserRepository;
    @Mock
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void importMembers_Success() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                "John Doe,john@example.com,9876543210,01-01-1990,123 Main St,Jane Doe,9876543211,1,,,\n";
        
        MockMultipartFile file = new MockMultipartFile("file", "members.csv", "text/csv", csvContent.getBytes());

        Branch branch = new Branch();
        branch.setBranchId(1L);

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(memberRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(memberRepository.existsByPhone("9876543210")).thenReturn(false);
        when(systemUserRepository.existsByEmail("john@example.com")).thenReturn(false);
        
        MemberDTO createdDto = new MemberDTO();
        createdDto.setMemberId(10L);
        when(memberService.createMember(any(MemberDTO.class))).thenReturn(createdDto);

        BulkImportReport report = csvImportService.importMembers(file, false);

        assertNotNull(report);
        assertEquals(1, report.getTotalRows());
        assertEquals(1, report.getSuccessCount());
        assertEquals("COMPLETED", report.getOverallStatus());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void importMembers_ValidationError() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                ",invalid-email,123,1990-01-01,123 Main St,Jane Doe,9876543211,1,,,\n";
        
        MockMultipartFile file = new MockMultipartFile("file", "members.csv", "text/csv", csvContent.getBytes());

        BulkImportReport report = csvImportService.importMembers(file, false);

        assertNotNull(report);
        assertEquals(1, report.getTotalRows());
        assertEquals(0, report.getSuccessCount());
        assertEquals(1, report.getValidationErrorCount());
        assertEquals(BulkImportRowResult.STATUS_VALIDATION_ERROR, report.getRowResults().get(0).getStatus());
    }

    @Test
    void importMembers_DuplicateEmail() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                "John Doe,john@example.com,9876543210,01-01-1990,123 Main St,Jane Doe,9876543211,1,,,\n";
        
        MockMultipartFile file = new MockMultipartFile("file", "members.csv", "text/csv", csvContent.getBytes());

        when(memberRepository.existsByEmail("john@example.com")).thenReturn(true);

        BulkImportReport report = csvImportService.importMembers(file, false);

        assertNotNull(report);
        assertEquals(1, report.getTotalRows());
        assertEquals(1, report.getDuplicateCount());
        assertEquals(BulkImportRowResult.STATUS_DUPLICATE, report.getRowResults().get(0).getStatus());
    }

    @Test
    void importMembers_BranchNotFound() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                "John Doe,john@example.com,9876543210,01-01-1990,123 Main St,Jane Doe,9876543211,999,,,\n";
        
        MockMultipartFile file = new MockMultipartFile("file", "members.csv", "text/csv", csvContent.getBytes());

        when(memberRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(memberRepository.existsByPhone("9876543210")).thenReturn(false);
        when(systemUserRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        BulkImportReport report = csvImportService.importMembers(file, false);

        assertNotNull(report);
        assertEquals(1, report.getTotalRows());
        assertEquals(1, report.getValidationErrorCount());
        assertTrue(report.getRowResults().get(0).getErrorMessage().contains("not found"));
    }
}

