package com.fitness.controller;

import com.fitness.dto.JwtResponse;
import com.fitness.dto.LoginRequest;
import com.fitness.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login and session management (US01)")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@Operation(summary = "Login and receive JWT token")
	public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.ok(authService.login(req));
	}
}
