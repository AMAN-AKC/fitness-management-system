package com.fitness.service;

import com.fitness.dto.ClassBookingDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.ClassBooking;
import com.fitness.entity.Classes;
import com.fitness.entity.Member;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.ClassBookingRepository;
import com.fitness.repository.ClassesRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.entity.Notification;
import com.fitness.entity.AuditLog;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassBookingServiceTest {

    @InjectMocks
    private ClassBookingService classBookingService;

    @Mock
    private ClassBookingRepository bookingRepo;

    @Mock
    private ClassesRepository classesRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private SystemUserRepository userRepo;

    @Mock
    private HealthConsentService healthConsentService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @Mock
    private ModelMapper mapper;

    @Test
    void bookClass_InactiveMember_ThrowsException() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        Classes cls = new Classes();
        Member member = new Member();
        member.setStatus(Member.Status.SUSPENDED);

        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> classBookingService.bookClass(dto));
    }

    @Test
    void bookClass_Success() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setStatus(Classes.Status.ACTIVE);
        cls.setBranch(branch);
        cls.setStartDate(LocalDate.now().plusDays(2));
        cls.setClassTime(LocalTime.of(10, 0));
        cls.setDurationMins(60);
        cls.setCapacity(20);

        SystemUser user = new SystemUser();
        user.setUserId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setStatus(Member.Status.ACTIVE);
        member.setHomeBranch(branch);
        member.setUser(user);

        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        Membership membership = new Membership();
        membership.setStatus(Membership.Status.ACTIVE);
        Plan plan = new Plan();
        plan.setBranches(Collections.singleton(branch));
        membership.setPlan(plan);
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(Arrays.asList(membership));
        when(bookingRepo.countByFitnessClassClassIdAndBookingStatus(eq(1L), any())).thenReturn(5L);
        when(bookingRepo.findByFitnessClassClassIdAndMemberMemberId(1L, 1L)).thenReturn(Optional.empty());
        
        ClassBooking booking = new ClassBooking();
        booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
        
        when(bookingRepo.save(any(ClassBooking.class))).thenReturn(booking);
        when(mapper.map(any(), eq(ClassBookingDTO.class))).thenReturn(dto);

        ClassBookingDTO result = classBookingService.bookClass(dto);

        assertNotNull(result);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void bookClass_ClassNotActive() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);
        Classes cls = new Classes();
        cls.setStatus(Classes.Status.CANCELLED);
        Member member = new Member();
        member.setStatus(Member.Status.ACTIVE);
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> classBookingService.bookClass(dto));
    }

    @Test
    void bookClass_DifferentBranch() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);
        Classes cls = new Classes();
        cls.setStatus(Classes.Status.ACTIVE);
        Branch b1 = new Branch(); b1.setBranchId(1L);
        Branch b2 = new Branch(); b2.setBranchId(2L);
        cls.setBranch(b1);
        Member member = new Member();
        member.setStatus(Member.Status.ACTIVE);
        member.setHomeBranch(b2);
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        assertThrows(BusinessRuleException.class, () -> classBookingService.bookClass(dto));
    }

    @Test
    void bookClass_Waitlisted() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setStatus(Classes.Status.ACTIVE);
        cls.setBranch(branch);
        cls.setStartDate(LocalDate.now().plusDays(2));
        cls.setClassTime(LocalTime.of(10, 0));
        cls.setDurationMins(60);
        cls.setCapacity(5); // full

        SystemUser user = new SystemUser();
        user.setUserId(1L);

        Member member = new Member();
        member.setMemberId(1L);
        member.setStatus(Member.Status.ACTIVE);
        member.setHomeBranch(branch);
        member.setUser(user);

        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        Membership membership = new Membership();
        membership.setStatus(Membership.Status.ACTIVE);
        Plan plan = new Plan();
        plan.setBranches(Collections.singleton(branch));
        membership.setPlan(plan);
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(Arrays.asList(membership));
        lenient().when(bookingRepo.countByFitnessClassClassIdAndBookingStatus(1L, ClassBooking.BookingStatus.CONFIRMED)).thenReturn(5L);
        lenient().when(bookingRepo.countByFitnessClassClassIdAndBookingStatus(1L, ClassBooking.BookingStatus.PENDING_CONFIRMATION)).thenReturn(0L);
        lenient().when(bookingRepo.countByFitnessClassClassIdAndBookingStatus(1L, ClassBooking.BookingStatus.WAITLISTED)).thenReturn(2L);
        
        ClassBooking saved = new ClassBooking();
        saved.setBookingStatus(ClassBooking.BookingStatus.WAITLISTED);
        saved.setWaitlistPosition(3);
        when(bookingRepo.save(any(ClassBooking.class))).thenReturn(saved);
        
        ClassBookingDTO retDto = new ClassBookingDTO();
        when(mapper.map(any(), eq(ClassBookingDTO.class))).thenReturn(retDto);
        
        assertNotNull(classBookingService.bookClass(dto));
    }

    @Test
    void cancelBooking_Success() {
        ClassBooking booking = new ClassBooking();
        booking.setBookingId(1L);
        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setStartDate(LocalDate.now().plusDays(2));
        cls.setClassTime(LocalTime.of(12, 0));
        booking.setFitnessClass(cls);
        Member member = new Member();
        member.setMemberId(1L);
        SystemUser user = new SystemUser();
        user.setUserId(1L);
        member.setUser(user);
        booking.setMember(member);
        
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        
        classBookingService.cancelBooking(1L);
        
        assertEquals(ClassBooking.BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingRepo, atLeastOnce()).save(booking);
    }

    @Test
    void markNoShow_Success() {
        ReflectionTestUtils.setField(classBookingService, "noShowPenaltyThreshold", 3);
        ClassBooking booking = new ClassBooking();
        booking.setBookingId(1L);
        booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
        Member member = new Member();
        member.setMemberId(1L);
        SystemUser user = new SystemUser();
        user.setUserId(1L);
        member.setUser(user);
        booking.setMember(member);
        
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepo.findByMemberMemberId(1L)).thenReturn(Collections.emptyList());
        
        classBookingService.markNoShow(1L);
        
        assertEquals(ClassBooking.BookingStatus.NO_SHOW, booking.getBookingStatus());
    }

    @Test
    void acceptWaitlistPromotion_Success() {
        ClassBooking booking = new ClassBooking();
        booking.setBookingId(1L);
        booking.setBookingStatus(ClassBooking.BookingStatus.PENDING_CONFIRMATION);
        Classes cls = new Classes();
        cls.setClassId(1L);
        booking.setFitnessClass(cls);
        Member member = new Member();
        member.setMemberId(1L);
        SystemUser user = new SystemUser();
        user.setUserId(1L);
        member.setUser(user);
        booking.setMember(member);
        
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        
        ClassBookingDTO retDto = new ClassBookingDTO();
        when(mapper.map(any(), eq(ClassBookingDTO.class))).thenReturn(retDto);
        when(bookingRepo.save(any(ClassBooking.class))).thenReturn(booking);
        
        ClassBookingDTO result = classBookingService.acceptWaitlistPromotion(1L);
        assertEquals(ClassBooking.BookingStatus.CONFIRMED, booking.getBookingStatus());
    }
    
    @Test
    void getBookingsByMember_Success() {
        when(bookingRepo.findByMemberMemberId(1L)).thenReturn(Collections.emptyList());
        List<ClassBookingDTO> result = classBookingService.getBookingsByMember(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void overrideBooking_Success() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);
        
        SystemUser overrideUser = new SystemUser();
        overrideUser.setUserId(2L);
        overrideUser.setUsername("admin");
        
        Classes cls = new Classes();
        cls.setClassId(1L);
        
        Member member = new Member();
        member.setMemberId(1L);
        
        when(userRepo.findById(2L)).thenReturn(Optional.of(overrideUser));
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        when(bookingRepo.findByFitnessClassClassIdAndMemberMemberId(1L, 1L)).thenReturn(Optional.empty());
        
        ClassBooking saved = new ClassBooking();
        saved.setBookingId(1L);
        when(bookingRepo.save(any())).thenReturn(saved);
        
        ClassBookingDTO retDto = new ClassBookingDTO();
        when(mapper.map(any(), eq(ClassBookingDTO.class))).thenReturn(retDto);
        
        assertNotNull(classBookingService.overrideBooking(dto, 2L, "reason"));
    }

    @Test
    void bookClass_PlanEligibility_WrongPlan() {
        ClassBookingDTO dto = new ClassBookingDTO();
        dto.setClassId(1L);
        dto.setMemberId(1L);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setStatus(Classes.Status.ACTIVE);
        cls.setBranch(branch);
        cls.setStartDate(LocalDate.now().plusDays(2));
        cls.setClassTime(LocalTime.of(10, 0));
        cls.setDurationMins(60);
        cls.setCapacity(20);
        cls.setPlanEligibility("PREMIUM");

        Member member = new Member();
        member.setMemberId(1L);
        member.setStatus(Member.Status.ACTIVE);
        member.setHomeBranch(branch);

        Plan plan = new Plan();
        plan.setEligibilityType(Plan.EligibilityType.STUDENT);
        Membership membership = new Membership();
        membership.setStatus(Membership.Status.ACTIVE);
        membership.setPlan(plan);

        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(healthConsentService.hasActiveConsent(1L)).thenReturn(true);
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE))
                .thenReturn(Arrays.asList(membership));

        assertThrows(BusinessRuleException.class, () -> classBookingService.bookClass(dto));
    }
}

