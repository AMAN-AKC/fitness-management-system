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
import java.time.LocalTime;
import java.util.Optional;

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
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

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
        when(bookingRepo.countByFitnessClassClassIdAndBookingStatus(eq(1L), any())).thenReturn(5L);
        when(bookingRepo.findByFitnessClassClassIdAndMemberMemberId(1L, 1L)).thenReturn(Optional.empty());
        
        ClassBooking booking = new ClassBooking();
        booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
        
        when(bookingRepo.save(any(ClassBooking.class))).thenReturn(booking);
        when(mapper.map(any(), eq(ClassBookingDTO.class))).thenReturn(dto);

        ClassBookingDTO result = classBookingService.bookClass(dto);

        assertNotNull(result);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
        verify(notificationService).sendNotification(any(), any(), any(), anyString(), anyString());
    }
}
