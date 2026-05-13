package com.fitness.controller;

import com.fitness.dto.FacilityDTO;
import com.fitness.service.FacilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
@Tag(name = "Facilities", description = "Room and facility management")
public class FacilityController {

	private final FacilityService facilityService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FacilityDTO> createFacility(@Valid @RequestBody FacilityDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.createFacility(dto));
	}

	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<FacilityDTO>> getFacilitiesByBranch(@PathVariable Long branchId) {
		return ResponseEntity.ok(facilityService.getFacilitiesByBranch(branchId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<FacilityDTO> getFacilityById(@PathVariable Long id) {
		return ResponseEntity.ok(facilityService.getFacilityById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FacilityDTO> updateFacility(@PathVariable Long id,
			@Valid @RequestBody FacilityDTO dto) {
		return ResponseEntity.ok(facilityService.updateFacility(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateFacility(@PathVariable Long id) {
		facilityService.deactivateFacility(id);
	}

	/**
	 * AC07: Toggle room maintenance mode — blocks bookings during downtime
	 */
	@PatchMapping("/{id}/maintenance")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<FacilityDTO> toggleMaintenance(@PathVariable Long id,
			@RequestParam boolean underMaintenance,
			@RequestParam(required = false) String reason) {
		return ResponseEntity.ok(facilityService.toggleMaintenance(id, underMaintenance, reason));
	}
}