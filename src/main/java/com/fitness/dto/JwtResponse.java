package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
	private String token;
	private String role;
	private Long userId;
	private String username;
}