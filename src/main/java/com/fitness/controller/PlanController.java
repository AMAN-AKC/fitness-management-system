package com.fitness.controller;

import com.fitness.dto.PlanDTO;
import com.fitness.service.PlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Plan catalog management (US03)")
public class PlanController {

	private final PlanService planService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PlanDTO> createPlan(@Valid @RequestBody PlanDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(dto));
	}

	@GetMapping
	public ResponseEntity<List<PlanDTO>> getActivePlans() {
		return ResponseEntity.ok(planService.getActivePlans());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PlanDTO> getPlanById(@PathVariable Long id) {
		return ResponseEntity.ok(planService.getPlanById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PlanDTO> updatePlan(@PathVariable Long id,
			@Valid @RequestBody PlanDTO dto) {
		return ResponseEntity.ok(planService.updatePlan(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivatePlan(@PathVariable Long id) {
		planService.deactivatePlan(id);
	}
}