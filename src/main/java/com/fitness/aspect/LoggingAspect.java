package com.fitness.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

	@Around("execution(* com.fitness.service.*.*(..))")
	public Object logServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
		String method = pjp.getSignature().toShortString();
		log.info(">> Entering: {}", method);
		long start = System.currentTimeMillis();
		try {
			Object result = pjp.proceed();
			log.info("<< Exiting: {} | time: {}ms",
					method, System.currentTimeMillis() - start);
			return result;
		} catch (Exception e) {
			log.error("!! Exception in: {} | msg: {}", method, e.getMessage());
			throw e;
		}
	}
}