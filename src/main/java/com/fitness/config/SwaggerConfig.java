package com.fitness.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	private static final String SCHEME_NAME = "BearerAuth";
	private static final String SCHEME_TYPE = "bearer";
	private static final String BEARER_FORMAT = "JWT";

	@Bean
	public OpenAPI fitnessOpenAPI() {
		return new OpenAPI()
				.info(apiInfo())
				.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
				.components(new Components()
						.addSecuritySchemes(SCHEME_NAME, securityScheme()));
	}

	private Info apiInfo() {
		return new Info()
				.title("Fitness Membership Management System API")
				.description(
						"REST API for the Fitness Membership Management System. " +
								"Supports Member, Front-Desk, Trainer, Manager, and Admin roles. " +
								"All secured endpoints require a valid JWT Bearer token.")
				.version("1.0.0")
				.contact(new Contact()
						.name("Group 17 — Infosys Internship")
						.email("group17@fitness.com"));
	}

	private SecurityScheme securityScheme() {
		return new SecurityScheme()
				.name(SCHEME_NAME)
				.type(SecurityScheme.Type.HTTP)
				.scheme(SCHEME_TYPE)
				.bearerFormat(BEARER_FORMAT)
				.description("Paste your JWT token here (without the 'Bearer ' prefix).");
	}
}