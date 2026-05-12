package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Report for bulk CSV import (AC10)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportReport {

	/**
	 * Total rows processed (excluding header)
	 */
	private int totalRows;

	/**
	 * Number of successful imports
	 */
	private int successCount;

	/**
	 * Number of duplicate records
	 */
	private int duplicateCount;

	/**
	 * Number of validation errors
	 */
	private int validationErrorCount;

	/**
	 * Number of system errors
	 */
	private int systemErrorCount;

	/**
	 * Timestamp when import was processed
	 */
	private LocalDateTime processedAt;

	/**
	 * File name that was imported
	 */
	private String fileName;

	/**
	 * Overall status: COMPLETED, PARTIAL, FAILED
	 */
	private String overallStatus;

	/**
	 * Detailed results per row
	 */
	@Builder.Default
	private List<BulkImportRowResult> rowResults = new ArrayList<>();

	/**
	 * Summary message for the import
	 */
	private String summary;

	/**
	 * Calculate overall status based on results
	 */
	public void calculateOverallStatus() {
		if (validationErrorCount == 0 && systemErrorCount == 0 && duplicateCount == 0) {
			this.overallStatus = "COMPLETED";
		} else if (successCount > 0) {
			this.overallStatus = "PARTIAL";
		} else {
			this.overallStatus = "FAILED";
		}
	}

	/**
	 * Generate summary message
	 */
	public void generateSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Import processed: ").append(successCount).append("/").append(totalRows).append(" success");

		if (duplicateCount > 0) {
			sb.append(", ").append(duplicateCount).append(" duplicates");
		}
		if (validationErrorCount > 0) {
			sb.append(", ").append(validationErrorCount).append(" validation errors");
		}
		if (systemErrorCount > 0) {
			sb.append(", ").append(systemErrorCount).append(" system errors");
		}

		this.summary = sb.toString();
	}
}
