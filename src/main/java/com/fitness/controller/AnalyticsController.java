package com.fitness.controller;

import com.fitness.dto.DashboardFilterDTO;
import com.fitness.dto.ManagerDashboardDto;
import com.fitness.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	@PostMapping("/dashboard")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'FRONT_DESK', 'TRAINER')")
	public ResponseEntity<ManagerDashboardDto> getDashboardStats(@RequestBody DashboardFilterDTO filter) {
		return ResponseEntity.ok(analyticsService.getDashboardStats(filter));
	}

	@PostMapping("/export")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public ResponseEntity<byte[]> exportAnalyticsCsv(@RequestBody DashboardFilterDTO filter) {
		ManagerDashboardDto stats = analyticsService.getDashboardStats(filter);
		StringBuilder csvContent = new StringBuilder("Month,Revenue,NewJoins,Churn\n");
		if (stats.getRevenueAnalytics() != null) {
			for (ManagerDashboardDto.RevenuePoint r : stats.getRevenueAnalytics()) {
				csvContent.append(r.getMonth()).append(",")
						.append(r.getRevenue()).append(",")
						.append(r.getNewJoins()).append(",")
						.append(r.getChurn()).append("\n");
			}
		}

		byte[] csvBytes = csvContent.toString().getBytes();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("text/csv"));
		headers.setContentDispositionFormData("attachment", "dashboard_metrics.csv");

		return ResponseEntity.ok()
				.headers(headers)
				.body(csvBytes);
	}

	@PostMapping("/reports/revenue")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public ResponseEntity<?> getRevenueReport(
			@RequestBody DashboardFilterDTO filter,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		// Mocked for drilldown, normally calls repository with Pageable
		return ResponseEntity.ok(java.util.Collections.emptyList());
	}
}
