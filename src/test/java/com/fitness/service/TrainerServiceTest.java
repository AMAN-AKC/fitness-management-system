package com.fitness.service;

import com.fitness.dto.TrainerDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.SystemUser;
import com.fitness.entity.Trainer;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.SystemUserRepository;
import com.fitness.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @InjectMocks
    private TrainerService trainerService;

    @Mock
    private TrainerRepository trainerRepo;
    @Mock
    private SystemUserRepository userRepo;
    @Mock
    private BranchRepository branchRepo;
    @Mock
    private ModelMapper mapper;

    private Trainer mockTrainer;
    private TrainerDTO mockDto;
    private SystemUser mockUser;
    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("john.trainer");
        mockUser.setFullName("John Trainer");

        mockBranch = new Branch();
        mockBranch.setBranchId(10L);

        mockTrainer = new Trainer();
        mockTrainer.setTrainerId(100L);
        mockTrainer.setUser(mockUser);
        mockTrainer.setBranch(mockBranch);
        mockTrainer.setBio("Experienced PT");
        mockTrainer.setIsActive(true);

        mockDto = new TrainerDTO();
        mockDto.setTrainerId(100L);
        mockDto.setUserId(1L);
        mockDto.setBranchId(10L);
        mockDto.setBio("Experienced PT");
    }

    @Test
    void createTrainer_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(branchRepo.findById(10L)).thenReturn(Optional.of(mockBranch));
        when(mapper.map(any(TrainerDTO.class), eq(Trainer.class))).thenReturn(mockTrainer);
        when(trainerRepo.save(any(Trainer.class))).thenReturn(mockTrainer);
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        TrainerDTO result = trainerService.createTrainer(mockDto);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John Trainer", result.getTrainerName());
        verify(trainerRepo).save(mockTrainer);
    }

    @Test
    void createTrainer_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trainerService.createTrainer(mockDto));
    }

    @Test
    void createTrainer_BranchNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(branchRepo.findById(10L)).thenReturn(Optional.empty());
        when(mapper.map(any(TrainerDTO.class), eq(Trainer.class))).thenReturn(mockTrainer);
        assertThrows(ResourceNotFoundException.class, () -> trainerService.createTrainer(mockDto));
    }

    @Test
    void getAllTrainers_Success() {
        when(trainerRepo.findAll()).thenReturn(Collections.singletonList(mockTrainer));
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        List<TrainerDTO> results = trainerService.getAllTrainers();
        assertEquals(1, results.size());
    }

    @Test
    void getTrainersByBranch_Success() {
        when(trainerRepo.findByBranchBranchId(10L)).thenReturn(Collections.singletonList(mockTrainer));
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        List<TrainerDTO> results = trainerService.getTrainersByBranch(10L);
        assertEquals(1, results.size());
    }

    @Test
    void getTrainerById_Success() {
        when(trainerRepo.findById(100L)).thenReturn(Optional.of(mockTrainer));
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        TrainerDTO result = trainerService.getTrainerById(100L);
        assertNotNull(result);
    }

    @Test
    void updateTrainer_Success() {
        when(trainerRepo.findById(100L)).thenReturn(Optional.of(mockTrainer));
        when(trainerRepo.save(any(Trainer.class))).thenReturn(mockTrainer);
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        TrainerDTO updateDto = new TrainerDTO();
        updateDto.setBio("Updated Bio");
        updateDto.setCertifications("ACE");
        updateDto.setAcceptingPtClients(false);

        TrainerDTO result = trainerService.updateTrainer(100L, updateDto);
        assertNotNull(result);
        assertEquals("Updated Bio", mockTrainer.getBio());
        assertFalse(mockTrainer.getAcceptingPtClients());
    }

    @Test
    void deactivateTrainer_Success() {
        when(trainerRepo.findById(100L)).thenReturn(Optional.of(mockTrainer));
        trainerService.deactivateTrainer(100L);
        assertFalse(mockTrainer.getIsActive());
        verify(trainerRepo).save(mockTrainer);
    }

    @Test
    void getTrainerByUserId_Success() {
        when(trainerRepo.findByUserUserId(1L)).thenReturn(Optional.of(mockTrainer));
        when(mapper.map(any(Trainer.class), eq(TrainerDTO.class))).thenReturn(mockDto);

        TrainerDTO result = trainerService.getTrainerByUserId(1L);
        assertNotNull(result);
    }

    @Test
    void getTrainerByUserId_NotFound() {
        when(trainerRepo.findByUserUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trainerService.getTrainerByUserId(1L));
    }
}
