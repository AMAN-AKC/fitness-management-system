package com.fitness.service;

import com.fitness.dto.AddOnDTO;
import com.fitness.entity.AddOn;
import com.fitness.entity.AuditLog;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.AddOnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddOnServiceTest {

    @InjectMocks
    private AddOnService addOnService;

    @Mock
    private AddOnRepository addOnRepo;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private AddOn mockAddOn;
    private AddOnDTO mockDto;

    @BeforeEach
    void setUp() {
        mockAddOn = new AddOn();
        mockAddOn.setAddonId(1L);
        mockAddOn.setAddonName("Locker");
        mockAddOn.setPrice(BigDecimal.valueOf(10));
        mockAddOn.setTaxPercent(BigDecimal.valueOf(5));
        mockAddOn.setIsActive(true);

        mockDto = new AddOnDTO();
        mockDto.setAddonId(1L);
        mockDto.setAddonName("Locker");
    }

    @Test
    void createAddOn_Success() {
        when(mapper.map(any(AddOnDTO.class), eq(AddOn.class))).thenReturn(mockAddOn);
        when(addOnRepo.save(any(AddOn.class))).thenReturn(mockAddOn);
        when(mapper.map(any(AddOn.class), eq(AddOnDTO.class))).thenReturn(mockDto);

        AddOnDTO result = addOnService.createAddOn(mockDto);

        assertNotNull(result);
        assertEquals("Locker", result.getAddonName());
        verify(addOnRepo).save(mockAddOn);
        verify(auditLogService).logForCurrentUser(eq("AddOn"), eq(1L), eq(AuditLog.Action.CREATE), anyString(), isNull());
    }

    @Test
    void getAllAddOns_Success() {
        when(addOnRepo.findAll()).thenReturn(Collections.singletonList(mockAddOn));
        when(mapper.map(any(AddOn.class), eq(AddOnDTO.class))).thenReturn(mockDto);

        List<AddOnDTO> results = addOnService.getAllAddOns();
        assertEquals(1, results.size());
    }

    @Test
    void getAddOnById_Success() {
        when(addOnRepo.findById(1L)).thenReturn(Optional.of(mockAddOn));
        when(mapper.map(any(AddOn.class), eq(AddOnDTO.class))).thenReturn(mockDto);

        AddOnDTO result = addOnService.getAddOnById(1L);
        assertNotNull(result);
    }

    @Test
    void getAddOnById_NotFound() {
        when(addOnRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> addOnService.getAddOnById(1L));
    }

    @Test
    void updateAddOn_Success() {
        when(addOnRepo.findById(1L)).thenReturn(Optional.of(mockAddOn));
        when(addOnRepo.save(any(AddOn.class))).thenReturn(mockAddOn);
        doNothing().when(mapper).map(any(AddOnDTO.class), any(AddOn.class));
        when(mapper.map(any(AddOn.class), eq(AddOnDTO.class))).thenReturn(mockDto);

        AddOnDTO result = addOnService.updateAddOn(1L, mockDto);
        
        assertNotNull(result);
        verify(mapper).map(mockDto, mockAddOn);
        verify(auditLogService).logForCurrentUser(eq("AddOn"), eq(1L), eq(AuditLog.Action.UPDATE), anyString(), anyString());
    }

    @Test
    void deactivateAddOn_Success() {
        when(addOnRepo.findById(1L)).thenReturn(Optional.of(mockAddOn));
        addOnService.deactivateAddOn(1L);
        
        assertFalse(mockAddOn.getIsActive());
        verify(addOnRepo).save(mockAddOn);
        verify(auditLogService).logForCurrentUser(eq("AddOn"), eq(1L), eq(AuditLog.Action.UPDATE), anyString(), anyString());
    }
}
