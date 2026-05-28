package com.fitness.aspect;

import com.fitness.annotation.FeatureRequired;
import com.fitness.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureFlagAspect {

	private final SystemConfigService configService;

	@Around("@annotation(featureRequired) || @within(featureRequired)")
	public Object checkFeatureFlag(ProceedingJoinPoint joinPoint, FeatureRequired featureRequired) throws Throwable {
		String featureName = featureRequired.value();
		if (!configService.isFeatureEnabled(featureName)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Feature " + featureName + " is currently disabled.");
		}
		return joinPoint.proceed();
	}
}
