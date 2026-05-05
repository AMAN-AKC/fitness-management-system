package com.fitness.controller;

import com.fitness.dto.MemberDTO;
import com.fitness.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member registration and management (US02)")
public class MemberController {

	private final MemberService memberService;

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
}