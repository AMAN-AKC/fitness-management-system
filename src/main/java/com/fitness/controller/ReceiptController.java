package com.fitness.controller;

import com.fitness.dto.ReceiptDTO;
import com.fitness.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Receipt management and issuance (US03)")
public class ReceiptController {

	private final ReceiptService receiptService;

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Get receipts for a member")
	public ResponseEntity<List<ReceiptDTO>> getMemberReceipts(@PathVariable Long memberId) {
		return ResponseEntity.ok(receiptService.getReceiptsByMember(memberId));
	}

	@GetMapping("/{receiptNumber}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Get receipt by number")
	public ResponseEntity<ReceiptDTO> getReceiptByNumber(@PathVariable String receiptNumber) {
		return ResponseEntity.ok(receiptService.getReceiptByNumber(receiptNumber));
	}

	@PostMapping("/{receiptId}/email")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Mark receipt as emailed")
	public ResponseEntity<ReceiptDTO> markAsEmailed(@PathVariable Long receiptId) {
		return ResponseEntity.ok(receiptService.markAsEmailed(receiptId));
	}
}
