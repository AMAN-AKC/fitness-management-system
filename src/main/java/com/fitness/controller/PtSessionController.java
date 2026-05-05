package com.fitness.controller;

import com.fitness.dto.PtSessionDTO;
import com.fitness.entity.PtSession;
import com.fitness.service.PtSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pt-sessions")
@RequiredArgsConstructor
@Tag(name = "PT Sessions", description = "Personal training booking (US08)")
public class PtSessionController {

	private final PtSessionService ptService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MEMBER','ADMIN')")
	public ResponseEntity<PtSessionDTO> requestSession(@Valid @RequestBody PtSessionDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ptService.requestSession(dto));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('TRAINER','ADMIN')")
	public ResponseEntity<PtSessionDTO> updateStatus(@PathVariable Long id,
			@RequestParam PtSession.Status status,
			@RequestParam(required = false) String notes) {
		return ResponseEntity.ok(ptService.updateStatus(id, status, notes));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','MANAGER','ADMIN')")
	public ResponseEntity<List<PtSessionDTO>> getSessionsByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(ptService.getSessionsByMember(memberId));
	}

	@GetMapping("/trainer/{trainerId}")
	@PreAuthorize("hasAnyRole('TRAINER','MANAGER','ADMIN')")
	public ResponseEntity<List<PtSessionDTO>> getSessionsByTrainer(@PathVariable Long trainerId) {
		return ResponseEntity.ok(ptService.getSessionsByTrainer(trainerId));
	}
}
