package com.fitness.controller;

import com.fitness.dto.PromoCodeDTO;
import com.fitness.service.PromoCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promo-codes")
@RequiredArgsConstructor
@Tag(name = "Promo Codes", description = "Promotions and referrals (US10)")
public class PromoCodeController {

	private final PromoCodeService promoService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PromoCodeDTO> createPromoCode(@Valid @RequestBody PromoCodeDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(promoService.createPromoCode(dto));
	}

	@GetMapping("/validate/{code}")
	public ResponseEntity<PromoCodeDTO> validateCode(@PathVariable String code) {
		return ResponseEntity.ok(promoService.validateAndGet(code));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PromoCodeDTO>> getAllPromoCodes() {
		return ResponseEntity.ok(promoService.getAllPromoCodes());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deactivatePromoCode(@PathVariable Long id) {
		promoService.deactivatePromoCode(id);
	}
}
