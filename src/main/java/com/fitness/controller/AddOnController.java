package com.fitness.controller;

import com.fitness.dto.AddOnDTO;
import com.fitness.service.AddOnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addons")
@RequiredArgsConstructor
@Tag(name = "Add-Ons", description = "Add-on management (US03)")
public class AddOnController {

	private final AddOnService addOnService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AddOnDTO> createAddOn(@Valid @RequestBody AddOnDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(addOnService.createAddOn(dto));
	}

	@GetMapping
	public ResponseEntity<List<AddOnDTO>> getAllAddOns() {
		return ResponseEntity.ok(addOnService.getAllAddOns());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AddOnDTO> getAddOnById(@PathVariable Long id) {
		return ResponseEntity.ok(addOnService.getAddOnById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AddOnDTO> updateAddOn(@PathVariable Long id,
			@Valid @RequestBody AddOnDTO dto) {
		return ResponseEntity.ok(addOnService.updateAddOn(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateAddOn(@PathVariable Long id) {
		addOnService.deactivateAddOn(id);
	}
}
