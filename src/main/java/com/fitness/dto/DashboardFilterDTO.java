package com.fitness.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DashboardFilterDTO {
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Long branchId;
	private Long planId;
	private Long trainerId;

}
