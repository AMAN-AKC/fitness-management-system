package com.fitness.service;

import com.fitness.dto.ClassesDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Classes;
import com.fitness.entity.Facility;
import com.fitness.entity.Trainer;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.ClassBookingRepository;
import com.fitness.repository.ClassesRepository;
import com.fitness.repository.FacilityRepository;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Map;

import com.fitness.entity.AuditLog;
import com.fitness.entity.ClassBooking;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.entity.SystemUser;
import com.fitness.entity.Notification;
import com.fitness.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassesServiceTest {

    @InjectMocks
    private ClassesService classesService;

    @Mock
    private ClassesRepository classesRepo;

    @Mock
    private TrainerRepository trainerRepo;

    @Mock
    private FacilityRepository facilityRepo;

    @Mock
    private BranchRepository branchRepo;

    @Mock
    private ClassBookingRepository bookingRepo;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ModelMapper mapper;

    @Test
    void createClass_Success() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(1L);
        dto.setClassTime("10:00");
        dto.setDurationMins(60);

        Trainer trainer = new Trainer();
        trainer.setTrainerId(1L);

        Facility room = new Facility();
        room.setFacilityId(1L);
        room.setUnderMaintenance(false);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setTrainer(trainer);
        cls.setRoom(room);
        cls.setBranch(branch);

        when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(room));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(classesRepo.findConflictingByRoom(eq(1L), any(LocalTime.class), any(LocalTime.class))).thenReturn(List.of());
        when(classesRepo.findConflictingByTrainer(eq(1L), any(LocalTime.class), any(LocalTime.class))).thenReturn(List.of());
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);

        ClassesDTO result = classesService.createClass(dto);

        assertNotNull(result);
        assertEquals(1L, result.getClassId());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void createClass_RoomUnderMaintenance_ThrowsException() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(1L);

        Trainer trainer = new Trainer();
        Facility room = new Facility();
        room.setUnderMaintenance(true);
        Branch branch = new Branch();

        when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(room));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));

        assertThrows(BusinessRuleException.class, () -> classesService.createClass(dto));
    }

    @Test
    void cancelClass_Success() {
        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setClassName("Yoga");
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);

        classesService.cancelClass(1L, "Trainer sick");

        assertEquals(Classes.Status.CANCELLED, cls.getStatus());
        assertEquals("Trainer sick", cls.getCancelReason());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void createClass_TrainerNotFound() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(99L);
        when(trainerRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> classesService.createClass(dto));
    }

    @Test
    void createClass_RoomNotFound() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(99L);
        when(trainerRepo.findById(1L)).thenReturn(Optional.of(new Trainer()));
        when(facilityRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> classesService.createClass(dto));
    }

    @Test
    void createClass_BranchNotFound() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(99L);
        when(trainerRepo.findById(1L)).thenReturn(Optional.of(new Trainer()));
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(new Facility()));
        when(branchRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> classesService.createClass(dto));
    }

    @Test
    void createClass_RoomConflict() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(1L);
        dto.setClassTime("10:00");
        dto.setDurationMins(60);
        
        Trainer trainer = new Trainer();
        trainer.setTrainerId(1L);
        Facility room = new Facility();
        room.setFacilityId(1L);
        Branch branch = new Branch();
        branch.setBranchId(1L);

        when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(room));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(classesRepo.findConflictingByRoom(eq(1L), any(LocalTime.class), any(LocalTime.class))).thenReturn(List.of(new Classes()));

        assertThrows(BusinessRuleException.class, () -> classesService.createClass(dto));
    }

    @Test
    void createClass_TrainerConflict() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(1L);
        dto.setRoomId(1L);
        dto.setBranchId(1L);
        dto.setClassTime("10:00");
        dto.setDurationMins(60);
        
        Trainer trainer = new Trainer();
        trainer.setTrainerId(1L);
        Facility room = new Facility();
        room.setFacilityId(1L);
        Branch branch = new Branch();
        branch.setBranchId(1L);

        when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(room));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(classesRepo.findConflictingByRoom(eq(1L), any(LocalTime.class), any(LocalTime.class))).thenReturn(Collections.emptyList());
        when(classesRepo.findConflictingByTrainer(eq(1L), any(LocalTime.class), any(LocalTime.class))).thenReturn(List.of(new Classes()));

        assertThrows(BusinessRuleException.class, () -> classesService.createClass(dto));
    }

    @Test
    void getAllClasses_Success() {
        Classes cls = new Classes();
        cls.setClassId(1L);
        when(classesRepo.findAll()).thenReturn(List.of(cls));
        List<ClassesDTO> res = classesService.getAllClasses();
        assertEquals(1, res.size());
    }

    @Test
    void getClassesByBranch_Success() {
        Classes cls = new Classes();
        cls.setClassId(1L);
        when(classesRepo.findByBranchBranchId(1L)).thenReturn(List.of(cls));
        List<ClassesDTO> res = classesService.getClassesByBranch(1L);
        assertEquals(1, res.size());
    }

    @Test
    void getCalendarClasses_Success() {
        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setStartDate(LocalDate.of(2023, 1, 1));
        cls.setEndDate(LocalDate.of(2023, 1, 31));
        when(classesRepo.findByBranchBranchId(1L)).thenReturn(List.of(cls));
        
        List<ClassesDTO> res = classesService.getCalendarClasses(1L, LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 20));
        assertEquals(1, res.size());
        
        List<ClassesDTO> res2 = classesService.getCalendarClasses(1L, LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31));
        assertEquals(0, res2.size());
    }

    @Test
    void getEligibleClassesForMember_NoMembership() {
        Classes c1 = new Classes(); c1.setPlanEligibility("ALL");
        Classes c2 = new Classes(); c2.setPlanEligibility("PREMIUM");
        when(classesRepo.findByBranchBranchId(1L)).thenReturn(List.of(c1, c2));
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(Collections.emptyList());
        
        List<ClassesDTO> res = classesService.getEligibleClassesForMember(1L, 1L);
        assertEquals(1, res.size());
    }

    @Test
    void getEligibleClassesForMember_WithMembership() {
        Classes c1 = new Classes(); c1.setPlanEligibility("ALL");
        Classes c2 = new Classes(); c2.setPlanEligibility("GENERAL");
        Classes c3 = new Classes(); c3.setPlanEligibility("BASIC");
        when(classesRepo.findByBranchBranchId(1L)).thenReturn(List.of(c1, c2, c3));
        
        Membership m = new Membership();
        Plan p = new Plan();
        p.setEligibilityType(Plan.EligibilityType.GENERAL);
        m.setPlan(p);
        when(membershipRepo.findByMemberMemberIdAndStatus(1L, Membership.Status.ACTIVE)).thenReturn(List.of(m));
        
        List<ClassesDTO> res = classesService.getEligibleClassesForMember(1L, 1L);
        assertEquals(2, res.size());
    }

    @Test
    void getClassById_Success() {
        Classes cls = new Classes(); cls.setClassId(1L);
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        ClassesDTO dto = classesService.getClassById(1L);
        assertEquals(1L, dto.getClassId());
    }
    
    @Test
    void updateClass_Success() {
        ClassesDTO dto = new ClassesDTO();
        dto.setTrainerId(2L);
        dto.setRoomId(2L);
        dto.setBranchId(2L);
        dto.setClassName("Updated Name");
        
        Classes cls = new Classes(); cls.setClassId(1L); cls.setClassName("Old");
        Trainer t = new Trainer(); t.setTrainerId(1L); cls.setTrainer(t);
        
        Trainer t2 = new Trainer(); t2.setTrainerId(2L);
        Facility r = new Facility(); r.setFacilityId(2L);
        Branch b = new Branch(); b.setBranchId(2L);
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(trainerRepo.findById(2L)).thenReturn(Optional.of(t2));
        when(facilityRepo.findById(2L)).thenReturn(Optional.of(r));
        when(branchRepo.findById(2L)).thenReturn(Optional.of(b));
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);
        
        ClassesDTO res = classesService.updateClass(1L, dto);
        assertNotNull(res);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void cancelClass_NoReason_Throws() {
        Classes cls = new Classes(); cls.setClassId(1L);
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        assertThrows(BusinessRuleException.class, () -> classesService.cancelClass(1L, ""));
    }

    @Test
    void cancelClass_WithBookings() {
        Classes cls = new Classes();
        cls.setClassId(1L);
        cls.setClassName("Yoga");
        
        ClassBooking cb = new ClassBooking();
        Member m = new Member();
        SystemUser u = new SystemUser();
        u.setUserId(10L);
        m.setUser(u);
        cb.setMember(m);
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);
        when(bookingRepo.findByFitnessClassClassIdAndBookingStatus(1L, ClassBooking.BookingStatus.CONFIRMED))
            .thenReturn(List.of(cb));
            
        classesService.cancelClass(1L, "Trainer sick");

        verify(notificationService).sendNotification(eq(10L), eq(Notification.NotifType.CANCELLATION), eq(Notification.Channel.IN_APP), anyString(), anyString());
    }

    @Test
    void substituteTrainer_Success() {
        Classes cls = new Classes(); cls.setClassId(1L); cls.setClassTime(LocalTime.of(10, 0)); cls.setDurationMins(60);
        Trainer t1 = new Trainer(); t1.setTrainerId(1L); cls.setTrainer(t1);
        
        Trainer t2 = new Trainer(); t2.setTrainerId(2L);
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(trainerRepo.findById(2L)).thenReturn(Optional.of(t2));
        when(classesRepo.findConflictingByTrainer(eq(2L), any(LocalTime.class), any(LocalTime.class))).thenReturn(Collections.emptyList());
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);
        
        ClassesDTO res = classesService.substituteTrainer(1L, 2L, "Sick");
        assertNotNull(res);
    }
    
    @Test
    void substituteTrainer_Conflict() {
        Classes cls = new Classes(); cls.setClassId(1L); cls.setClassTime(LocalTime.of(10, 0)); cls.setDurationMins(60);
        Trainer t1 = new Trainer(); t1.setTrainerId(1L); cls.setTrainer(t1);
        
        Trainer t2 = new Trainer(); t2.setTrainerId(2L);
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(trainerRepo.findById(2L)).thenReturn(Optional.of(t2));
        
        Classes conflict = new Classes(); conflict.setClassId(99L);
        when(classesRepo.findConflictingByTrainer(eq(2L), any(LocalTime.class), any(LocalTime.class))).thenReturn(List.of(conflict));
        
        assertThrows(BusinessRuleException.class, () -> classesService.substituteTrainer(1L, 2L, "Sick"));
    }

    @Test
    void exportClassesAsCsv_Success() {
        Classes cls = new Classes(); cls.setClassId(1L);
        Trainer t = new Trainer(); t.setTrainerId(1L); cls.setTrainer(t);
        Facility f = new Facility(); f.setFacilityId(1L); cls.setRoom(f);
        Branch b = new Branch(); b.setBranchId(1L); cls.setBranch(b);
        cls.setStatus(Classes.Status.ACTIVE);
        when(classesRepo.findAll()).thenReturn(List.of(cls));
        
        byte[] bytes = classesService.exportClassesAsCsv();
        assertTrue(bytes.length > 0);
    }

    @Test
    void getClassesByTrainer_Success() {
        Classes cls = new Classes(); cls.setClassId(1L);
        when(classesRepo.findByTrainerTrainerId(1L)).thenReturn(List.of(cls));
        List<ClassesDTO> res = classesService.getClassesByTrainer(1L);
        assertEquals(1, res.size());
    }

    @Test
    void importClassesFromCsv_Success() throws Exception {
        String csvContent = "className,trainerId,roomId,branchId,startDate,endDate,weekdays,classTime,durationMins,capacity\n" +
                "Yoga,1,1,1,2023-01-01,2023-12-31,Mon,10:00,60,20";
        MockMultipartFile file = new MockMultipartFile("file", "classes.csv", "text/csv", csvContent.getBytes());
        
        Trainer trainer = new Trainer(); trainer.setTrainerId(1L);
        Facility room = new Facility(); room.setFacilityId(1L);
        Branch branch = new Branch(); branch.setBranchId(1L);
        Classes cls = new Classes(); cls.setClassId(10L);
        
        lenient().when(trainerRepo.findById(1L)).thenReturn(Optional.of(trainer));
        lenient().when(facilityRepo.findById(1L)).thenReturn(Optional.of(room));
        lenient().when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        lenient().when(classesRepo.findConflictingByRoom(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        lenient().when(classesRepo.findConflictingByTrainer(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        lenient().when(classesRepo.save(any(Classes.class))).thenReturn(cls);
        
        List<Map<String, Object>> res = classesService.importClassesFromCsv(file);
        assertEquals(1, res.size());
        assertEquals("SUCCESS", res.get(0).get("status"));
    }
}
