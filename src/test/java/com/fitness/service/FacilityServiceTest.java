package com.fitness.service;

import com.fitness.dto.FacilityDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Branch;
import com.fitness.entity.Facility;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.FacilityRepository;
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
public class FacilityServiceTest {

    @InjectMocks
    private FacilityService facilityService;

    @Mock
    private FacilityRepository facilityRepo;
    @Mock
    private BranchRepository branchRepo;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private Facility mockFacility;
    private FacilityDTO mockDto;
    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        mockBranch = new Branch();
        mockBranch.setBranchId(10L);

        mockFacility = new Facility();
        mockFacility.setFacilityId(1L);
        mockFacility.setFacilityName("Yoga Studio");
        mockFacility.setBranch(mockBranch);
        mockFacility.setIsActive(true);
        mockFacility.setUnderMaintenance(false);

        mockDto = new FacilityDTO();
        mockDto.setFacilityId(1L);
        mockDto.setFacilityName("Yoga Studio");
        mockDto.setBranchId(10L);
    }

    @Test
    void createFacility_Success() {
        when(branchRepo.findById(10L)).thenReturn(Optional.of(mockBranch));
        when(mapper.map(any(FacilityDTO.class), eq(Facility.class))).thenReturn(mockFacility);
        when(facilityRepo.save(any(Facility.class))).thenReturn(mockFacility);
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        FacilityDTO result = facilityService.createFacility(mockDto);

        assertNotNull(result);
        assertEquals("Yoga Studio", result.getFacilityName());
        verify(facilityRepo).save(mockFacility);
    }

    @Test
    void createFacility_BranchNotFound() {
        when(branchRepo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> facilityService.createFacility(mockDto));
    }

    @Test
    void getFacilitiesByBranch_Success() {
        when(facilityRepo.findByBranchBranchId(10L)).thenReturn(Collections.singletonList(mockFacility));
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        List<FacilityDTO> results = facilityService.getFacilitiesByBranch(10L);
        assertEquals(1, results.size());
    }

    @Test
    void getAllFacilities_Success() {
        when(facilityRepo.findAll()).thenReturn(Collections.singletonList(mockFacility));
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        List<FacilityDTO> results = facilityService.getAllFacilities();
        assertEquals(1, results.size());
    }

    @Test
    void getFacilityById_Success() {
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        FacilityDTO result = facilityService.getFacilityById(1L);
        assertNotNull(result);
    }

    @Test
    void updateFacility_Success() {
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(facilityRepo.save(any(Facility.class))).thenReturn(mockFacility);
        doNothing().when(mapper).map(any(FacilityDTO.class), any(Facility.class));
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        FacilityDTO result = facilityService.updateFacility(1L, mockDto);
        assertNotNull(result);
        verify(mapper).map(mockDto, mockFacility);
    }

    @Test
    void deactivateFacility_Success() {
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(mockFacility));
        facilityService.deactivateFacility(1L);
        assertFalse(mockFacility.getIsActive());
        verify(facilityRepo).save(mockFacility);
    }

    @Test
    void toggleMaintenance_Enable() {
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(facilityRepo.save(any(Facility.class))).thenReturn(mockFacility);
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        FacilityDTO result = facilityService.toggleMaintenance(1L, true, "Cleaning");

        assertNotNull(result);
        assertTrue(mockFacility.getUnderMaintenance());
        assertEquals("Cleaning", mockFacility.getMaintenanceReason());
        verify(auditLogService).logForCurrentUser(eq("Facility"), eq(1L), eq(AuditLog.Action.UPDATE), isNull(), anyString());
    }

    @Test
    void toggleMaintenance_Disable() {
        mockFacility.setUnderMaintenance(true);
        when(facilityRepo.findById(1L)).thenReturn(Optional.of(mockFacility));
        when(facilityRepo.save(any(Facility.class))).thenReturn(mockFacility);
        when(mapper.map(any(Facility.class), eq(FacilityDTO.class))).thenReturn(mockDto);

        FacilityDTO result = facilityService.toggleMaintenance(1L, false, null);

        assertNotNull(result);
        assertFalse(mockFacility.getUnderMaintenance());
        assertNull(mockFacility.getMaintenanceReason());
        verify(auditLogService).logForCurrentUser(eq("Facility"), eq(1L), eq(AuditLog.Action.UPDATE), isNull(), anyString());
    }
}
