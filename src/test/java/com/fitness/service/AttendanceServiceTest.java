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
    @Test
    void checkIn_CrossBranch_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(2L);

        Branch memberBranch = new Branch();
        memberBranch.setBranchId(1L);

        Branch checkInBranch = new Branch();
        checkInBranch.setBranchId(2L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(memberBranch);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(2L)).thenReturn(Optional.of(checkInBranch));

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(dto));
    }

    @Test
    void checkIn_Duplicate_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);

        Membership membership = new Membership();
        
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(dto));
    }

    @Test
    void checkIn_SuspendedMember_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.SUSPENDED);

        Membership membership = new Membership();
        
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        lenient().when(invoiceRepo.findByStatus(any())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(dto));
    }

    @Test
    void checkIn_DeactivatedMember_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.DEACTIVATED);

        Membership membership = new Membership();
        
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        lenient().when(invoiceRepo.findByStatus(any())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> attendanceService.checkIn(dto));
    }

    @Test
    void checkIn_WithClassId_Success() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);
        dto.setClassId(10L);
        dto.setScanMethod(Attendance.ScanMethod.CARD);

        Branch branch = new Branch();
        branch.setBranchId(1L);
        branch.setBranchName("Test Branch");

        Member member = new Member();
        member.setMemberId(1L);
        member.setMemName("Test Member");
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.ACTIVE);

        Membership membership = new Membership();
        
        com.fitness.entity.Classes cls = new com.fitness.entity.Classes();
        cls.setClassId(10L);
        cls.setClassName("Yoga");

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        when(classesRepo.findById(10L)).thenReturn(Optional.of(cls));
        lenient().when(invoiceRepo.findByStatus(any())).thenReturn(List.of());
        
        Attendance attendance = new Attendance();
        attendance.setFitnessClass(cls);
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(dto);
        when(healthConsentService.getConsentStatus(1L)).thenReturn(Map.of("consentRequired", false));

        AttendanceDTO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        verify(attendanceRepo).save(any(Attendance.class));
    }

    @Test
    void checkIn_WithUnpaidDuesAndConsentRequired_AlertFlagTrue() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);
        branch.setBranchName("Test");

        Member member = new Member();
        member.setMemberId(1L);
        member.setMemName("Name");
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.ACTIVE);

        Membership membership = new Membership();
        
        com.fitness.entity.Invoice invoice = new com.fitness.entity.Invoice();
        invoice.setMember(member);
        invoice.setOutstanding(new java.math.BigDecimal("100.00"));

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        when(invoiceRepo.findByStatus(com.fitness.entity.Invoice.Status.OVERDUE)).thenReturn(List.of(invoice));
        when(healthConsentService.getConsentStatus(1L)).thenReturn(Map.of("consentRequired", true));
        
        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(dto);

        AttendanceDTO result = attendanceService.checkIn(dto);

        assertTrue(result.getAlertFlag());
    }

    @Test
    void checkIn_AutoDetectClass_Success() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch);
        member.setStatus(Member.Status.ACTIVE);

        Membership membership = new Membership();
        
        com.fitness.entity.Classes cls = new com.fitness.entity.Classes();
        cls.setClassId(10L);
        cls.setStatus(com.fitness.entity.Classes.Status.ACTIVE);
        cls.setClassTime(java.time.LocalTime.now()); // Right now, should hit window

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(membership));
        when(attendanceRepo.existsByMemberMemberIdAndCheckInTimeBetween(eq(1L), any(), any())).thenReturn(false);
        lenient().when(invoiceRepo.findByStatus(any())).thenReturn(List.of());
        when(healthConsentService.getConsentStatus(1L)).thenReturn(Map.of("consentRequired", false));
        when(classesRepo.findByBranchBranchId(1L)).thenReturn(List.of(cls));
        
        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(dto);

        AttendanceDTO result = attendanceService.checkIn(dto);

        assertNotNull(result);
        verify(attendanceRepo).save(any(Attendance.class));
    }

    @Test
    void overrideCheckIn_NoReason_ThrowsException() {
        AttendanceDTO dto = new AttendanceDTO();
        assertThrows(BusinessRuleException.class, () -> attendanceService.overrideCheckIn(dto, 1L, ""));
    }

    @Test
    void overrideCheckIn_Success() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);
        dto.setClassId(10L);

        Branch branch = new Branch();
        branch.setBranchId(1L);
        
        Member member = new Member();
        member.setMemberId(1L);
        member.setMemName("Name");
        
        com.fitness.entity.SystemUser user = new com.fitness.entity.SystemUser();
        user.setUserId(2L);
        user.setUsername("admin");
        
        com.fitness.entity.Classes cls = new com.fitness.entity.Classes();

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));
        when(classesRepo.findById(10L)).thenReturn(Optional.of(cls));
        
        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        AttendanceDTO result = attendanceService.overrideCheckIn(dto, 2L, "System error");

        assertNotNull(result);
    }

    @Test
    void markClassAttendance_CrossBranch_ThrowsException() {
        Branch branch1 = new Branch();
        branch1.setBranchId(1L);
        Branch branch2 = new Branch();
        branch2.setBranchId(2L);
        
        Member member = new Member();
        member.setMemberId(1L);
        member.setHomeBranch(branch1);

        com.fitness.entity.Classes cls = new com.fitness.entity.Classes();

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(2L)).thenReturn(Optional.of(branch2));
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));

        assertThrows(BusinessRuleException.class, () -> attendanceService.markClassAttendance(1L, 1L, 2L));
    }

    @Test
    void queueOfflineCheckIn_Success() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setMemberId(1L);
        dto.setBranchId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);
        
        Member member = new Member();
        member.setMemberId(1L);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        
        Attendance attendance = new Attendance();
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(attendance);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        AttendanceDTO result = attendanceService.queueOfflineCheckIn(dto);

        assertNotNull(result);
    }

    @Test
    void syncPendingCheckIns_Success() {
        Attendance a = new Attendance();
        when(attendanceRepo.findBySyncStatus(Attendance.SyncStatus.PENDING)).thenReturn(List.of(a));
        when(attendanceRepo.save(any(Attendance.class))).thenReturn(a);
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        List<AttendanceDTO> result = attendanceService.syncPendingCheckIns();

        assertEquals(1, result.size());
    }

    @Test
    void getMemberCheckInFlags_Success() {
        Member member = new Member();
        member.setMemberId(1L);
        member.setMemName("Name");
        member.setStatus(Member.Status.ACTIVE);
        member.setNotes("Has notes");
        
        com.fitness.entity.Invoice invoice = new com.fitness.entity.Invoice();
        invoice.setMember(member);
        invoice.setOutstanding(new java.math.BigDecimal("50.0"));

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(invoiceRepo.findByStatus(com.fitness.entity.Invoice.Status.OVERDUE)).thenReturn(List.of(invoice));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(new Membership()));
        when(healthConsentService.getConsentStatus(1L)).thenReturn(Map.of("consentRequired", true));

        Map<String, Object> result = attendanceService.getMemberCheckInFlags(1L);
        
        assertEquals(1L, result.get("memberId"));
        assertTrue((Boolean)result.get("hasUnpaidDues"));
        assertTrue((Boolean)result.get("hasActiveMembership"));
        assertTrue((Boolean)result.get("hasHealthNotes"));
        assertEquals(true, result.get("consentRequired"));
    }

    @Test
    void getAttendanceByMember_Success() {
        Attendance a = new Attendance();
        when(attendanceRepo.findByMemberMemberId(1L)).thenReturn(List.of(a));
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        List<AttendanceDTO> result = attendanceService.getAttendanceByMember(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getTodayAttendanceByBranch_Success() {
        Attendance a = new Attendance();
        Branch b = new Branch();
        b.setBranchId(1L);
        a.setBranch(b);
        when(attendanceRepo.findByCheckInTimeBetween(any(), any())).thenReturn(List.of(a));
        when(mapper.map(any(), eq(AttendanceDTO.class))).thenReturn(new AttendanceDTO());

        List<AttendanceDTO> result = attendanceService.getTodayAttendanceByBranch(1L);
        assertEquals(1, result.size());
    }

    @Test
    void exportDailyAttendanceCsv_Success() {
        Attendance a = new Attendance();
        a.setLogId(1L);
        Branch b = new Branch();
        b.setBranchId(1L);
        b.setBranchName("B1");
        a.setBranch(b);
        Member m = new Member();
        m.setMemberId(1L);
        m.setMemName("M1");
        a.setMember(m);
        a.setCheckInTime(java.time.LocalDateTime.now());
        a.setScanMethod(Attendance.ScanMethod.QR);
        a.setAlertFlag(false);
        a.setSyncStatus(Attendance.SyncStatus.SYNCED);

        when(attendanceRepo.findByCheckInTimeBetween(any(), any())).thenReturn(List.of(a));

        byte[] result = attendanceService.exportDailyAttendanceCsv(1L);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
