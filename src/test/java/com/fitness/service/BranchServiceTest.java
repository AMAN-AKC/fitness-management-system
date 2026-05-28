package com.fitness.service;

import com.fitness.dto.BranchDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BranchServiceTest {

    @InjectMocks
    private BranchService branchService;

    @Mock
    private BranchRepository branchRepo;
    @Mock
    private MemberRepository memberRepo;
    @Mock
    private ModelMapper mapper;

    private Branch mockBranch;
    private BranchDTO mockDto;

    @BeforeEach
    void setUp() {
        mockBranch = new Branch();
        mockBranch.setBranchId(1L);
        mockBranch.setBranchName("Downtown Branch");
        mockBranch.setIsActive(true);

        mockDto = new BranchDTO();
        mockDto.setBranchId(1L);
        mockDto.setBranchName("Downtown Branch");
    }

    @Test
    void createBranch_Success() {
        when(branchRepo.existsByBranchName("Downtown Branch")).thenReturn(false);
        when(mapper.map(any(BranchDTO.class), eq(Branch.class))).thenReturn(mockBranch);
        when(branchRepo.save(any(Branch.class))).thenReturn(mockBranch);
        when(mapper.map(any(Branch.class), eq(BranchDTO.class))).thenReturn(mockDto);
        when(memberRepo.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, 1L)).thenReturn(100L);

        BranchDTO result = branchService.createBranch(mockDto);

        assertNotNull(result);
        assertEquals("Downtown Branch", result.getBranchName());
        assertEquals(100, result.getActiveMembersCount());
        verify(branchRepo).save(mockBranch);
    }

    @Test
    void createBranch_DuplicateName_ThrowsException() {
        when(branchRepo.existsByBranchName("Downtown Branch")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> branchService.createBranch(mockDto));
    }

    @Test
    void getAllBranches_Success() {
        when(branchRepo.findAll()).thenReturn(Collections.singletonList(mockBranch));
        when(mapper.map(any(Branch.class), eq(BranchDTO.class))).thenReturn(mockDto);

        List<BranchDTO> results = branchService.getAllBranches();
        assertEquals(1, results.size());
    }

    @Test
    void getActiveBranches_Success() {
        when(branchRepo.findByIsActiveTrue()).thenReturn(Collections.singletonList(mockBranch));
        when(mapper.map(any(Branch.class), eq(BranchDTO.class))).thenReturn(mockDto);

        List<BranchDTO> results = branchService.getActiveBranches();
        assertEquals(1, results.size());
    }

    @Test
    void getBranchById_Success() {
        when(branchRepo.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(mapper.map(any(Branch.class), eq(BranchDTO.class))).thenReturn(mockDto);

        BranchDTO result = branchService.getBranchById(1L);
        assertNotNull(result);
    }

    @Test
    void getBranchById_NotFound() {
        when(branchRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> branchService.getBranchById(1L));
    }

    @Test
    void updateBranch_Success() {
        when(branchRepo.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(branchRepo.save(any(Branch.class))).thenReturn(mockBranch);
        doNothing().when(mapper).map(any(BranchDTO.class), any(Branch.class));
        when(mapper.map(any(Branch.class), eq(BranchDTO.class))).thenReturn(mockDto);

        BranchDTO result = branchService.updateBranch(1L, mockDto);
        assertNotNull(result);
        verify(mapper).map(mockDto, mockBranch);
    }

    @Test
    void deactivateBranch_Success() {
        when(branchRepo.findById(1L)).thenReturn(Optional.of(mockBranch));
        branchService.deactivateBranch(1L);
        assertFalse(mockBranch.getIsActive());
        verify(branchRepo).save(mockBranch);
    }
}
