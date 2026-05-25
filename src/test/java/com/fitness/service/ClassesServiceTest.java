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
import java.util.List;
import java.util.Optional;

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
        
        when(classesRepo.findById(1L)).thenReturn(Optional.of(cls));
        when(classesRepo.save(any(Classes.class))).thenReturn(cls);

        classesService.cancelClass(1L, "Trainer sick");

        assertEquals(Classes.Status.CANCELLED, cls.getStatus());
        assertEquals("Trainer sick", cls.getCancelReason());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }
}
