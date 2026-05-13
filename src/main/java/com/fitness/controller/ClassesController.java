package com.fitness.controller;

import com.fitness.dto.ClassesDTO;
import com.fitness.service.ClassesService;
import com.fitness.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

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

	/**
	 * AC06: Cancel class with mandatory reason — notifies booked members
	 */
	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Cancel a class with reason (notifies booked members)")
	public ResponseEntity<Void> cancelClass(@PathVariable Long id,
			@RequestParam String reason) {
		classesService.cancelClass(id, reason);
		return ResponseEntity.noContent().build();
	}

	/**
	 * AC05: Substitute trainer — checks conflict, notifies booked members
	 */
	@PatchMapping("/{id}/substitute-trainer")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Substitute trainer for a class (notifies booked members)")
	public ResponseEntity<ClassesDTO> substituteTrainer(@PathVariable Long id,
			@RequestParam Long newTrainerId,
			@RequestParam String reason) {
		return ResponseEntity.ok(classesService.substituteTrainer(id, newTrainerId, reason));
	}

	/**
	 * AC09: Export all class schedules as CSV
	 */
	@GetMapping("/export/csv")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Export all class schedules as CSV")
	public ResponseEntity<byte[]> exportClassesCsv() {
		byte[] csv = classesService.exportClassesAsCsv();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"class_schedules.csv\"")
				.contentType(MediaType.TEXT_PLAIN)
				.body(csv);
	}

	/**
	 * AC09: Import class schedules from CSV
	 */
	@PostMapping("/import/csv")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Import class schedules from CSV with row-level validation")
	public ResponseEntity<Map<String, Object>> importClassesCsv(
			@RequestParam("file") MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new BusinessRuleException("CSV file is required");
		if (!file.getOriginalFilename().toLowerCase().endsWith(".csv"))
			throw new BusinessRuleException("File must be a CSV file (*.csv)");

		List<Map<String, Object>> results = classesService.importClassesFromCsv(file);
		long successCount = results.stream().filter(r -> "SUCCESS".equals(r.get("status"))).count();
		long errorCount = results.size() - successCount;

		return ResponseEntity.ok(Map.of(
				"status", "success",
				"totalRows", results.size(),
				"successCount", successCount,
				"errorCount", errorCount,
				"rowResults", results));
	}
}