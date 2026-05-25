package com.fitness.service;

import com.fitness.dto.BulkImportReport;
import com.fitness.dto.BulkImportRowResult;
import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void importMembers_Success() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                "John Doe,john@test.com,9876543210,1990-01-01,123 Main St,Jane Doe,9876543211,1,,,";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        Branch branch = new Branch();
        branch.setBranchId(1L);

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByPhone(anyString())).thenReturn(false);
        when(systemUserRepository.existsByEmail(anyString())).thenReturn(false);
        
        MemberDTO createdMember = new MemberDTO();
        createdMember.setMemberId(1L);
        when(memberService.createMember(any(MemberDTO.class))).thenReturn(createdMember);

        BulkImportReport report = csvImportService.importMembers(file);

        assertNotNull(report);
        assertEquals(1, report.getSuccessCount());
        assertEquals(0, report.getValidationErrorCount());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void importMembers_ValidationError() throws Exception {
        String csvContent = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n" +
                ",john@test.com,invalid_phone,invalid_date,,,,,,,,";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        BulkImportReport report = csvImportService.importMembers(file);

        assertNotNull(report);
        assertEquals(0, report.getSuccessCount());
        assertEquals(1, report.getValidationErrorCount());
        assertEquals(BulkImportRowResult.STATUS_VALIDATION_ERROR, report.getRowResults().get(0).getStatus());
    }
}
