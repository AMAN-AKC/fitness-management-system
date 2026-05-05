package com.fitness.controller;

import com.fitness.dto.TrainerDTO;
import com.fitness.service.TrainerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers", description = "Trainer profiles (US08)")
public class TrainerController {

	private final TrainerService trainerService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<TrainerDTO> createTrainer(@Valid @RequestBody TrainerDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.createTrainer(dto));
	}

	@GetMapping
	public ResponseEntity<List<TrainerDTO>> getAllTrainers() {
		return ResponseEntity.ok(trainerService.getAllTrainers());
	}

	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<TrainerDTO>> getTrainersByBranch(@PathVariable Long branchId) {
		return ResponseEntity.ok(trainerService.getTrainersByBranch(branchId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable Long id) {
		return ResponseEntity.ok(trainerService.getTrainerById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
	public ResponseEntity<TrainerDTO> updateTrainer(@PathVariable Long id,
			@Valid @RequestBody TrainerDTO dto) {
		return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateTrainer(@PathVariable Long id) {
		trainerService.deactivateTrainer(id);
	}
}