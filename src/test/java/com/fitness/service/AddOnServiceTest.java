package com.fitness.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AddOnServiceTest {

    @InjectMocks
    private AddOnService addOnService;

    @Test
    void testContextLoads() {
        assertNotNull(addOnService);
    }
}
