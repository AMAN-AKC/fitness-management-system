package com.fitness.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PtSessionServiceTest {

    @InjectMocks
    private PtSessionService ptSessionService;

    @Test
    void testContextLoads() {
        assertNotNull(ptSessionService);
    }
}
