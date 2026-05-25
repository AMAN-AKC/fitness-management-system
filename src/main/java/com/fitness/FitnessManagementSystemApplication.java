package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.CacheManager;

@SpringBootApplication(scanBasePackages = "com.fitness")
@EnableCaching
@EnableAspectJAutoProxy
@EnableScheduling
public class FitnessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessManagementSystemApplication.class, args);
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("managerDashboard");
	}
}
