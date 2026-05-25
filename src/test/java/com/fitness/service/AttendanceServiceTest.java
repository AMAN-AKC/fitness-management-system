package com.fitness.service;

import com.fitness.dto.AttendanceDTO;
import com.fitness.entity.Attendance;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.AttendanceRepository;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.ClassesRepository;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @InjectMocks
    private AttendanceService attendanceService;

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private BranchRepository branchRepo;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private ClassesRepository classesRepo;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private SystemUserRepository userRepo;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private HealthConsentService healthConsentService;

    @Mock
    private ModelMapper mapper;

    @Test
    void checkIn_NoActiveMembership_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(dto));
    }

    @Test
    void checkIn_Success() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);
        dto.setScanMethod(Attendance.ScanMethod.QR);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.ACTIVE);

        Membership membership = new Membership();
        
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        when(invoiceRepo.findByStatus(any())).thenReturn(List.of());
        when(healthConsentService.getConsentStatus(1L)).thenReturn(Map.of("consentRequired", false));
        
        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(dto);

        AttendanceDTO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void markClassAttendance_Success() {
        Branch branch = new Branch();
        branch.setBranchId(1L);
        
        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);

        com.fitness.entity.Classes cls = new com.fitness.entity.Classes();

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));

        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        AttendanceDTO result = attendanceService.markClassAttendance(1L, 1L, 1L);

        assertNotNull(result);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }
}
