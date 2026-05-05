package com.fitness.controller;

import com.fitness.dto.SystemUserDTO;
import com.fitness.service.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "System Users", description = "RBAC user management (US14)")
public class SystemUserController {

	private final SystemUserService userService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a system user (Admin only)")
	public ResponseEntity<SystemUserDTO> createUser(
			@Valid @RequestBody SystemUserDTO dto,
			@RequestParam String password) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto, password));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<SystemUserDTO>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
	public ResponseEntity<SystemUserDTO> getUserById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getUserById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<SystemUserDTO> updateUser(@PathVariable Long id,
			@Valid @RequestBody SystemUserDTO dto) {
		return ResponseEntity.ok(userService.updateUser(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateUser(@PathVariable Long id) {
		userService.deactivateUser(id);
	}
}
