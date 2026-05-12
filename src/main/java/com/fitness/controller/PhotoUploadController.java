package com.fitness.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fitness.entity.Member;
import com.fitness.entity.AuditLog;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MemberRepository;
import com.fitness.service.AuditLogService;
import com.fitness.service.FileUploadService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
public class PhotoUploadController {

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AuditLogService auditLogService;

	/**
	 * Upload photo/ID document for a member
	 * 
	 * POST /api/v1/members/{id}/photo
	 * 
	 * @param id   Member ID
	 * @param file Photo/ID file (jpg, png, pdf, max 5MB)
	 * @return Response with file path and upload status
	 */
	@PostMapping("/{id}/photo")
	@PreAuthorize("hasAnyRole('FRONT_DESK', 'ADMIN')")
	public ResponseEntity<Map<String, Object>> uploadPhoto(
			@PathVariable Long id,
			@RequestParam("file") MultipartFile file) {

		// Verify member exists
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

		try {
			// Get previous photo path for audit purposes
			String previousPhotoPath = member.getPhotoPath();

			// Validate and store file
			String newPhotoPath = fileUploadService.uploadPhoto(file, id);

			// Update member with new photo path
			member.setPhotoPath(newPhotoPath);
			memberRepository.save(member);

			// Audit log the file upload
			String previousValue = previousPhotoPath != null ? previousPhotoPath : "null";
			auditLogService.log(
					member.getMemberId(),
					"Member",
					member.getMemberId(),
					AuditLog.Action.UPDATE,
					"photoPath=" + previousValue,
					"photoPath=" + newPhotoPath);

			// Build response
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("success", true);
			responseData.put("message", "Photo uploaded successfully");
			responseData.put("memberId", member.getMemberId());
			responseData.put("fileName", file.getOriginalFilename());
			responseData.put("filePath", newPhotoPath);
			responseData.put("fileSize", file.getSize());

			return ResponseEntity.status(HttpStatus.CREATED).body(responseData);

		} catch (Exception e) {
			// Audit log the failed attempt
			auditLogService.log(
					member.getMemberId(),
					"Member",
					member.getMemberId(),
					AuditLog.Action.UPDATE,
					"photoPath=" + member.getPhotoPath(),
					"photoUploadFailed=reason:" + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("error", e.getMessage());

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	/**
	 * Get upload policy and allowed file types
	 * 
	 * GET /api/v1/members/photo/policy
	 * 
	 * @return Upload policy information
	 */
	@GetMapping("/photo/policy")
	@PreAuthorize("hasAnyRole('FRONT_DESK', 'MEMBER', 'ADMIN')")
	public ResponseEntity<Map<String, String>> getUploadPolicy() {
		Map<String, String> policy = new HashMap<>();
		policy.put("policy", fileUploadService.getUploadPolicy());
		policy.put("allowedTypes", "jpg, jpeg, png, pdf");
		policy.put("maxSizeMB", "5");

		return ResponseEntity.ok(policy);
	}

	/**
	 * Delete member's photo (admin only)
	 * 
	 * DELETE /api/v1/members/{id}/photo
	 * 
	 * @param id Member ID
	 * @return Success response
	 */
	@DeleteMapping("/{id}/photo")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> deletePhoto(@PathVariable Long id) {

		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

		String previousPhotoPath = member.getPhotoPath();

		if (previousPhotoPath != null) {
			fileUploadService.deletePhoto(previousPhotoPath);
		}

		member.setPhotoPath(null);
		memberRepository.save(member);

		// Audit log
		auditLogService.log(
				member.getMemberId(),
				"Member",
				member.getMemberId(),
				AuditLog.Action.UPDATE,
				"photoPath=" + previousPhotoPath,
				"photoPath=null");

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("success", true);
		responseData.put("message", "Photo deleted successfully");
		responseData.put("memberId", member.getMemberId());

		return ResponseEntity.ok(responseData);
	}
}
