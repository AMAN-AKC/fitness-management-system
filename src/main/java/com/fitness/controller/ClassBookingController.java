package com.fitness.controller;

import com.fitness.dto.ClassBookingDTO;
import com.fitness.service.ClassBookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Class Bookings", description = "Booking, cancellation, and waitlist (US06)")
public class ClassBookingController {

	private final ClassBookingService bookingService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	public ResponseEntity<ClassBookingDTO> bookClass(@Valid @RequestBody ClassBookingDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookClass(dto));
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelBooking(@PathVariable Long id) {
		bookingService.cancelBooking(id);
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<ClassBookingDTO>> getBookingsByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(bookingService.getBookingsByMember(memberId));
	}

	@GetMapping("/class/{classId}")
	@PreAuthorize("hasAnyRole('TRAINER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<ClassBookingDTO>> getBookingsByClass(@PathVariable Long classId) {
		return ResponseEntity.ok(bookingService.getBookingsByClass(classId));
	}
}