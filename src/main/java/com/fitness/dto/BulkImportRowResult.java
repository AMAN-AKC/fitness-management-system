package com.fitness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result for a single row in CSV import (AC10)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportRowResult {

	/**
	 * Row number in CSV (1-indexed from header)
	 */
	private int rowNumber;

	/**
	 * Status: SUCCESS, DUPLICATE, VALIDATION_ERROR, SYSTEM_ERROR
	 */
	private String status;

	/**
	 * Error message if status is not SUCCESS
	 */
	private String errorMessage;

	/**
	 * Member ID if created successfully
	 */
	private Long memberId;

	/**
	 * Email of the member being imported
	 */
	private String email;

	/**
	 * Phone of the member being imported
	 */
	private String phone;

	/**
	 * Member name
	 */
	private String memberName;

	/**
	 * Field-level validation errors
	 */
	private String fieldErrors;

	// Constants for status
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_DUPLICATE = "DUPLICATE";
	public static final String STATUS_VALIDATION_ERROR = "VALIDATION_ERROR";
	public static final String STATUS_SYSTEM_ERROR = "SYSTEM_ERROR";
}
