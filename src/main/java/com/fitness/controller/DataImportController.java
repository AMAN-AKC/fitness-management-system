package com.fitness.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fitness.dto.BulkImportReport;
import com.fitness.service.CsvImportService;
import com.fitness.exception.BusinessRuleException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/import")
@Tag(name = "Data Import", description = "Bulk import data from CSV files (AC10)")
public class DataImportController {

	@Autowired
	private CsvImportService csvImportService;

	/**
	 * Import members from CSV file (AC10)
	 * 
	 * POST /api/v1/import/members/csv
	 * 
	 * CSV format (with header row):
	 * memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes
	 * 
	 * Example:
	 * John Doe,john@example.com,9876543210,1990-05-15,123 Main St,Jane
	 * Doe,9876543211,1,,
	 * 
	 * @param file CSV file to import (multipart form)
	 * @return BulkImportReport with detailed results per row
	 */
	@PostMapping("/members/csv")
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Operation(summary = "Import members from CSV file with validation report")
	public ResponseEntity<Map<String, Object>> importMembersFromCsv(
			@RequestParam("file") MultipartFile file) {

		if (file == null || file.isEmpty()) {
			throw new BusinessRuleException("CSV file is required");
		}

		if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
			throw new BusinessRuleException("File must be a CSV file (*.csv)");
		}

		// Process CSV file
		BulkImportReport report = csvImportService.importMembers(file);

		// Build response
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", report.getSummary());
		response.put("fileName", report.getFileName());
		response.put("processedAt", report.getProcessedAt());
		response.put("overallStatus", report.getOverallStatus());
		response.put("totalRows", report.getTotalRows());
		response.put("successCount", report.getSuccessCount());
		response.put("duplicateCount", report.getDuplicateCount());
		response.put("validationErrorCount", report.getValidationErrorCount());
		response.put("systemErrorCount", report.getSystemErrorCount());
		response.put("rowResults", report.getRowResults());

		HttpStatus status = report.getOverallStatus().equals("FAILED")
				? HttpStatus.BAD_REQUEST
				: HttpStatus.OK;

		return ResponseEntity.status(status).body(response);
	}

	/**
	 * Get CSV import template with headers
	 * 
	 * GET /api/v1/import/members/template
	 * 
	 * @return CSV template as text
	 */
	@GetMapping("/members/template")
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Operation(summary = "Download CSV template for member import")
	public ResponseEntity<String> downloadImportTemplate() {
		String template = "memName,email,phone,dob,address,emgContact,emgPhone,homeBranchId,referralCode,corporateCode,notes\n"
				+
				"John Doe,john@example.com,9876543210,1990-05-15,123 Main St,Jane Doe,9876543211,1,,\n" +
				"Jane Smith,jane@example.com,9876543212,1992-03-20,456 Oak Ave,John Smith,9876543213,1,,High value client\n";

		return ResponseEntity
				.ok()
				.header("Content-Disposition", "attachment; filename=\"member_import_template.csv\"")
				.header("Content-Type", "text/csv")
				.body(template);
	}

	/**
	 * Get import validation rules
	 * 
	 * GET /api/v1/import/members/rules
	 * 
	 * @return Validation rules for import
	 */
	@GetMapping("/members/rules")
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Operation(summary = "Get CSV import validation rules")
	public ResponseEntity<Map<String, Object>> getImportRules() {
		Map<String, Object> rules = new HashMap<>();

		Map<String, String> fieldRules = new HashMap<>();
		fieldRules.put("memName", "Required, max 120 characters");
		fieldRules.put("email", "Required, must be valid email format, unique across system");
		fieldRules.put("phone", "Required, must be 10-digit Indian format starting with 6-9, unique");
		fieldRules.put("dob", "Required, format: yyyy-MM-dd (e.g., 1990-05-15)");
		fieldRules.put("address", "Required, text field");
		fieldRules.put("emgContact", "Required, emergency contact name");
		fieldRules.put("emgPhone", "Required, must be 10-digit Indian format starting with 6-9");
		fieldRules.put("homeBranchId", "Required, valid branch ID from system");
		fieldRules.put("referralCode", "Optional, max 30 characters");
		fieldRules.put("corporateCode", "Optional, max 30 characters");
		fieldRules.put("notes", "Optional, unlimited text");

		rules.put("fields", fieldRules);
		rules.put("maxFileSize", "5MB");
		rules.put("fileFormat", "CSV with header row");
		rules.put("defaultStatus", "PROSPECT (until plan purchased)");
		rules.put("transactions", "Atomicity: All rows succeed or partial success with error details");
		rules.put("successCriteria",
				"Duplicate check: Email/phone must be unique. Validation: All required fields present and properly formatted.");

		return ResponseEntity.ok(rules);
	}
}
