package com.fitness.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
    }

    @Test
    void logBefore_Success() {
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1"});

        assertDoesNotThrow(() -> loggingAspect.logBefore(joinPoint));
    }

    @Test
    void logAfterReturning_Success() {
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");

        assertDoesNotThrow(() -> loggingAspect.logAfterReturning(joinPoint, "result"));
    }

    @Test
    void logAfterThrowing_Success() {
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");

        assertDoesNotThrow(() -> loggingAspect.logAfterThrowing(joinPoint, new RuntimeException("Error")));
    }

    @Test
    void logExecutionTime_Success() throws Throwable {
        when(proceedingJoinPoint.getTarget()).thenReturn(new Object());
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(proceedingJoinPoint.proceed()).thenReturn("Success");

        Object result = loggingAspect.logExecutionTime(proceedingJoinPoint);

        assertEquals("Success", result);
        verify(proceedingJoinPoint).proceed();
    }
}
