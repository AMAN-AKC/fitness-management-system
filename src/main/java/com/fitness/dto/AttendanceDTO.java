package com.fitness.dto;

import com.fitness.entity.Attendance;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO {
	private Long logId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	@NotNull(message = "Please provide a valid branch")
	private Long branchId;

	private String checkInTime;
	private String checkOutTime;
	private Boolean alertFlag;

	@NotNull(message = "Please provide a valid scan method")
	private Attendance.ScanMethod scanMethod;

	private Attendance.SyncStatus syncStatus;
	private Long classId;
	private Long overrideBy;
	private String overrideReason;
}
