package com.fitness.service;

import com.fitness.entity.FeatureFlag;
import com.fitness.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureFlagServiceTest {

    @InjectMocks
    private FeatureFlagService featureFlagService;

    @Mock
    private FeatureFlagRepository repository;

    private FeatureFlag mockFlag;

    @BeforeEach
    void setUp() {
        mockFlag = new FeatureFlag();
        mockFlag.setFlagId(1L);
        mockFlag.setFlagName("NEW_UI");
        mockFlag.setEnabled(false);
    }

    @Test
    void getAllFlags_Success() {
        when(repository.findAll()).thenReturn(Collections.singletonList(mockFlag));
        List<FeatureFlag> results = featureFlagService.getAllFlags();
        assertEquals(1, results.size());
    }

    @Test
    void updateFlag_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockFlag));
        when(repository.save(any(FeatureFlag.class))).thenReturn(mockFlag);

        FeatureFlag result = featureFlagService.updateFlag(1L, true, "ADMIN");

        assertNotNull(result);
        assertTrue(mockFlag.isEnabled());
        assertEquals("ADMIN", mockFlag.getLastModifiedBy());
        verify(repository).save(mockFlag);
    }

    @Test
    void updateFlag_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> featureFlagService.updateFlag(1L, true, "ADMIN"));
    }
}
