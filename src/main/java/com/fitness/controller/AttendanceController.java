package com.fitness.controller;

import com.fitness.dto.AttendanceDTO;
import com.fitness.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Check-in and attendance logging (US07)")
public class AttendanceController {

	private final AttendanceService attendanceService;

	@PostMapping("/checkin")
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	public ResponseEntity<AttendanceDTO> checkIn(@Valid @RequestBody AttendanceDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(dto));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<AttendanceDTO>> getAttendanceByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(attendanceService.getAttendanceByMember(memberId));
	}

	@GetMapping("/branch/{branchId}/today")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<AttendanceDTO>> getTodayAttendance(@PathVariable Long branchId) {
		return ResponseEntity.ok(attendanceService.getTodayAttendanceByBranch(branchId));
	}
}
