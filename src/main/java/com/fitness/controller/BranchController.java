package com.fitness.controller;

import com.fitness.dto.BranchDTO;
import com.fitness.service.BranchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Multi-branch management (US15)")
public class BranchController {

	private final BranchService branchService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<BranchDTO> createBranch(@Valid @RequestBody BranchDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(dto));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','FRONT_DESK','TRAINER','MEMBER')")
	public ResponseEntity<List<BranchDTO>> getActiveBranches() {
		return ResponseEntity.ok(branchService.getActiveBranches());
	}

	@GetMapping("/{id}")
	public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
		return ResponseEntity.ok(branchService.getBranchById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<BranchDTO> updateBranch(@PathVariable Long id,
			@Valid @RequestBody BranchDTO dto) {
		return ResponseEntity.ok(branchService.updateBranch(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateBranch(@PathVariable Long id) {
		branchService.deactivateBranch(id);
	}
}