package com.fitness.dto;

import com.fitness.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemUserDTO {
	private Long userId;

	@NotBlank(message = "Please provide a valid username")
	@Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
	private String username;

	@NotBlank(message = "Please provide a valid email")
	@Email(message = "Please provide a valid email")
	private String email;

	private Role role;
	private Boolean isActive;
}