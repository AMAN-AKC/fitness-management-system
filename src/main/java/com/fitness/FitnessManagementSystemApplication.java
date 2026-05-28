package com.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.fitness")
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
public class FitnessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessManagementSystemApplication.class, args);
	}
}
