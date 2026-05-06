package com.fitness.fitness_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "com.fitness")
@EnableJpaRepositories(basePackages = "com.fitness.repository")
@EntityScan(basePackages = "com.fitness.entity")
@EnableAspectJAutoProxy
public class FitnessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessManagementSystemApplication.class, args);
	}

}
