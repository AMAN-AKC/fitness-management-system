package com.fitness.controller;

import com.fitness.dto.ClassesDTO;
import com.fitness.service.ClassesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Class scheduling (US05)")
public class ClassesController {

	private final ClassesService classesService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<ClassesDTO> createClass(@Valid @RequestBody ClassesDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(classesService.createClass(dto));
	}

	@GetMapping
	public ResponseEntity<List<ClassesDTO>> getAllClasses() {
		return ResponseEntity.ok(classesService.getAllClasses());
	}

	@GetMapping("/branch/{branchId}")
	public ResponseEntity<List<ClassesDTO>> getClassesByBranch(@PathVariable Long branchId) {
		return ResponseEntity.ok(classesService.getClassesByBranch(branchId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClassesDTO> getClassById(@PathVariable Long id) {
		return ResponseEntity.ok(classesService.getClassById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<ClassesDTO> updateClass(@PathVariable Long id,
			@Valid @RequestBody ClassesDTO dto) {
		return ResponseEntity.ok(classesService.updateClass(id, dto));
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<Void> cancelClass(@PathVariable Long id,
			@RequestParam String reason) {
		classesService.cancelClass(id, reason);
		return ResponseEntity.noContent().build();
	}
}