package com.fitness.controller;

import com.fitness.entity.BranchInventory;
import com.fitness.repository.BranchInventoryRepository;
import com.fitness.service.MemberTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchAdminController {

	private final MemberTransferService transferService;
	private final BranchInventoryRepository inventoryRepo;

	@PostMapping("/transfer-member")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<Void> transferMember(
			@RequestParam Long memberId,
			@RequestParam Long targetBranchId,
			@RequestParam String reason) {
		transferService.transferMember(memberId, targetBranchId, reason);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{branchId}/inventory")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
	public ResponseEntity<List<BranchInventory>> getBranchInventory(@PathVariable Long branchId) {
		return ResponseEntity.ok(inventoryRepo.findByBranch_BranchId(branchId));
	}

	@PostMapping("/{branchId}/inventory")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<BranchInventory> addInventory(
			@PathVariable Long branchId,
			@RequestBody BranchInventory inventory) {
		// Mock setup for now, ideally handled via a service
		return ResponseEntity.ok(inventoryRepo.save(inventory));
	}
}
