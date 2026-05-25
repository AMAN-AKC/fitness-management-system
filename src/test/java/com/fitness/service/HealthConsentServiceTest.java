package com.fitness.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HealthConsentServiceTest {

    @InjectMocks
    private HealthConsentService healthConsentService;

    @Test
    void testContextLoads() {
        assertNotNull(healthConsentService);
    }
}
