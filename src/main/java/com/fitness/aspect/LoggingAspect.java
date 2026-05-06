package com.fitness.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

	// Pointcut — all methods in every service class
	@Pointcut("execution(* com.fitness.service.*.*(..))")
	public void serviceLayer() {
	}

	// ── Log method entry ───────────────────────────────────────────────────
	@Before("serviceLayer()")
	public void logBefore(JoinPoint jp) {
		log.info("[ENTER] {}.{}() | args={}",
				jp.getTarget().getClass().getSimpleName(),
				jp.getSignature().getName(),
				Arrays.toString(jp.getArgs()));
	}

	// ── Log method exit + return value ─────────────────────────────────────
	@AfterReturning(pointcut = "serviceLayer()", returning = "result")
	public void logAfterReturning(JoinPoint jp, Object result) {
		log.info("[EXIT]  {}.{}() | returned={}",
				jp.getTarget().getClass().getSimpleName(),
				jp.getSignature().getName(),
				result);
	}

	// ── Log exceptions ─────────────────────────────────────────────────────
	@AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
	public void logAfterThrowing(JoinPoint jp, Throwable ex) {
		log.error("[ERROR] {}.{}() | exception={} | message={}",
				jp.getTarget().getClass().getSimpleName(),
				jp.getSignature().getName(),
				ex.getClass().getSimpleName(),
				ex.getMessage());
	}

	// ── Log execution time ─────────────────────────────────────────────────
	@Around("serviceLayer()")
	public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
		Object result = pjp.proceed();
		long elapsed = System.currentTimeMillis() - start;
		log.info("[TIME]  {}.{}() | elapsed={}ms",
				pjp.getTarget().getClass().getSimpleName(),
				pjp.getSignature().getName(),
				elapsed);
		return result;
	}
}