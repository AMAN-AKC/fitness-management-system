package com.fitness.controller;

import com.fitness.dto.MemberDTO;
import com.fitness.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member registration and management (US02)")
public class MemberController {

	private final MemberService memberService;

	@Value("${file.upload.directory:uploads/photos}")
	private String uploadDirectory;

	@Value("${file.upload.max-size:5242880}")
	private long maxFileSize;

	@Value("${file.allowed-types:jpg,jpeg,png,pdf}")
	private String allowedTypes;

	@PostMapping
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	@Operation(summary = "Register a new member")
	public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody MemberDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(dto));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<MemberDTO>> getAllMembers() {
		return ResponseEntity.ok(memberService.getAllMembers());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
		return ResponseEntity.ok(memberService.getMemberById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id,
			@Valid @RequestBody MemberDTO dto) {
		return ResponseEntity.ok(memberService.updateMember(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivateMember(@PathVariable Long id) {
		memberService.deactivateMember(id);
	}

	@GetMapping("/branch/{branchId}")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<MemberDTO>> getMembersByBranch(@PathVariable Long branchId) {
		return ResponseEntity.ok(memberService.getMembersByBranch(branchId));
	}

	/**
	 * AC06: Upload photo/ID for a member with type and size validation.
	 */
	@PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	@Operation(summary = "Upload photo/ID for a member")
	public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
			@RequestParam("file") MultipartFile file) throws IOException {

		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
		}

		if (file.getSize() > maxFileSize) {
			return ResponseEntity.badRequest().body(Map.of("message",
					"File exceeds maximum size of " + (maxFileSize / 1024 / 1024) + "MB"));
		}

		String originalName = file.getOriginalFilename();
		String extension = originalName != null && originalName.contains(".")
				? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
				: "";
		List<String> allowed = Arrays.asList(allowedTypes.split(","));
		if (!allowed.contains(extension)) {
			return ResponseEntity.badRequest().body(Map.of("message",
					"File type '" + extension + "' not allowed. Allowed: " + allowedTypes));
		}

		Path uploadPath = Paths.get(uploadDirectory);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		String fileName = "member_" + id + "_" + System.currentTimeMillis() + "." + extension;
		Path filePath = uploadPath.resolve(fileName);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		MemberDTO updated = memberService.updatePhotoPath(id, filePath.toString());
		return ResponseEntity.ok(updated);
	}

	/**
	 * AC10: Bulk import members via CSV with row-level validation report.
	 */
	@PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	@Operation(summary = "Bulk import members from CSV")
	public ResponseEntity<List<Map<String, String>>> bulkUpload(
			@RequestParam("file") MultipartFile file) {

		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(List.of(
					Map.of("row", "0", "status", "FAILED", "message", "File is empty")));
		}

		String fileName = file.getOriginalFilename();
		if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
			return ResponseEntity.badRequest().body(List.of(
					Map.of("row", "0", "status", "FAILED", "message", "Only CSV files are accepted")));
		}

		List<Map<String, String>> report = memberService.bulkUploadMembers(file);
		return ResponseEntity.ok(report);
	}
}