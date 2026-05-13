package com.fitness.controller;

import com.fitness.dto.AttendanceDTO;
import com.fitness.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Check-in and attendance logging (US07)")
public class AttendanceController {

	private final AttendanceService attendanceService;

	/**
	 * AC01: Member check-in via QR/card/manual lookup.
	 */
	@PostMapping("/checkin")
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	public ResponseEntity<AttendanceDTO> checkIn(@Valid @RequestBody AttendanceDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(dto));
	}

	/**
	 * AC09: Staff override check-in with reason.
	 */
	@PostMapping("/checkin/override")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Override check-in rules (staff only, requires justification)")
	public ResponseEntity<AttendanceDTO> overrideCheckIn(
			@Valid @RequestBody AttendanceDTO dto,
			@RequestParam Long overrideByUserId,
			@RequestParam String reason) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(attendanceService.overrideCheckIn(dto, overrideByUserId, reason));
	}

	/**
	 * AC08: Trainer marks attendance from class roster.
	 */
	@PostMapping("/class/{classId}/mark")
	@PreAuthorize("hasAnyRole('TRAINER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Trainer marks member attendance from class roster")
	public ResponseEntity<AttendanceDTO> markClassAttendance(
			@PathVariable Long classId,
			@RequestParam Long memberId,
			@RequestParam Long branchId) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(attendanceService.markClassAttendance(memberId, classId, branchId));
	}

	/**
	 * AC05: Get check-in flags (unpaid dues, health notes).
	 */
	@GetMapping("/member/{memberId}/flags")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Get check-in flags: unpaid dues, health notes, membership status")
	public ResponseEntity<Map<String, Object>> getMemberFlags(@PathVariable Long memberId) {
		return ResponseEntity.ok(attendanceService.getMemberCheckInFlags(memberId));
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

	/**
	 * AC07: Export daily attendance report as CSV.
	 */
	@GetMapping("/branch/{branchId}/today/csv")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Export today's attendance report as CSV")
	public ResponseEntity<byte[]> exportDailyAttendanceCsv(@PathVariable Long branchId) {
		byte[] csv = attendanceService.exportDailyAttendanceCsv(branchId);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance_today.csv\"")
				.contentType(MediaType.TEXT_PLAIN)
				.body(csv);
	}

	/**
	 * AC06: Queue offline check-in (stub).
	 */
	@PostMapping("/checkin/offline")
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	@Operation(summary = "Queue check-in for offline sync (PENDING status)")
	public ResponseEntity<AttendanceDTO> offlineCheckIn(@Valid @RequestBody AttendanceDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(attendanceService.queueOfflineCheckIn(dto));
	}

	/**
	 * AC06: Sync all pending check-ins.
	 */
	@PostMapping("/sync")
	@PreAuthorize("hasAnyRole('FRONT_DESK','ADMIN')")
	@Operation(summary = "Sync all pending offline check-ins")
	public ResponseEntity<List<AttendanceDTO>> syncPending() {
		return ResponseEntity.ok(attendanceService.syncPendingCheckIns());
	}
}
