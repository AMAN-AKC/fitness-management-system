package com.fitness.controller;

import com.fitness.dto.PaymentDTO;
import com.fitness.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and dunning (US04, US11)")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody PaymentDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(dto));
	}

	@PatchMapping("/{id}/refund")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<PaymentDTO> refundPayment(@PathVariable Long id,
			@RequestParam Long refundBy,
			@RequestParam String reason) {
		return ResponseEntity.ok(paymentService.refundPayment(id, refundBy, reason));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<PaymentDTO>> getPaymentsByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(paymentService.getPaymentsByMember(memberId));
	}

	@GetMapping("/failed")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<PaymentDTO>> getFailedPayments() {
		return ResponseEntity.ok(paymentService.getFailedPayments());
	}

	@GetMapping("/revenue/mtd")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<BigDecimal> getRevenueMTD() {
		return ResponseEntity.ok(paymentService.getRevenueMTD());
	}
}