package com.fitness.aspect;

import com.fitness.annotation.FeatureRequired;
import com.fitness.service.SystemConfigService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeatureFlagAspectTest {

    @InjectMocks
    private FeatureFlagAspect featureFlagAspect;

    @Mock
    private SystemConfigService configService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private FeatureRequired featureRequired;

    @Test
    void checkFeatureFlag_FeatureEnabled_Proceeds() throws Throwable {
        when(featureRequired.value()).thenReturn("NEW_UI");
        when(configService.isFeatureEnabled("NEW_UI")).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("Success");

        Object result = featureFlagAspect.checkFeatureFlag(joinPoint, featureRequired);

        assertEquals("Success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void checkFeatureFlag_FeatureDisabled_ThrowsException() throws Throwable {
        when(featureRequired.value()).thenReturn("NEW_UI");
        when(configService.isFeatureEnabled("NEW_UI")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            featureFlagAspect.checkFeatureFlag(joinPoint, featureRequired);
        });

        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("disabled"));
        verify(joinPoint, never()).proceed();
    }
}
