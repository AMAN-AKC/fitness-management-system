package com.fitness.fitness_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.fitness")
@EnableCaching
@EnableJpaRepositories(basePackages = "com.fitness.repository")
@EntityScan(basePackages = "com.fitness.entity")
@EnableAspectJAutoProxy
@EnableScheduling
public class FitnessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessManagementSystemApplication.class, args);
	}

}
