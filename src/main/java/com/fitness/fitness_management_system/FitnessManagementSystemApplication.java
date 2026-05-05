package com.fitness.fitness_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class FitnessManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitnessManagementSystemApplication.class, args);
	}

}
