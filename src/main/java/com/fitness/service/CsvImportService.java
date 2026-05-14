package com.fitness.service;

import com.fitness.dto.BulkImportReport;
import com.fitness.dto.BulkImportRowResult;
import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CsvImportService {

	private final MemberService memberService;
	private final BranchRepository branchRepository;
	private final MemberRepository memberRepository;
	private final SystemUserRepository systemUserRepository;
	private final AuditLogService auditLogService;

	// Validation patterns
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * Parse and validate CSV file for bulk member import (AC10)
	 * 
	 * @param file CSV file to import
	 * @return BulkImportReport with per-row results and summary
	 */
	@Transactional(rollbackFor = Exception.class)
	public BulkImportReport importMembers(MultipartFile file) {
		BulkImportReport report = BulkImportReport.builder()
				.fileName(file.getOriginalFilename())
				.processedAt(LocalDateTime.now())
				.rowResults(new ArrayList<>())
				.build();

		CSVFormat format = CSVFormat.DEFAULT.builder()
				.setHeader()
				.setSkipHeaderRecord(true)
				.setIgnoreEmptyLines(true)
				.build();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
				CSVParser csvParser = new CSVParser(reader, format)) {
			int rowNumber = 1;

			for (CSVRecord record : csvParser) {
				rowNumber++;
				BulkImportRowResult rowResult = processRow(record, rowNumber);
				report.getRowResults().add(rowResult);

				// Update counters
				switch (rowResult.getStatus()) {
					case BulkImportRowResult.STATUS_SUCCESS:
						report.setSuccessCount(report.getSuccessCount() + 1);
						break;
					case BulkImportRowResult.STATUS_DUPLICATE:
						report.setDuplicateCount(report.getDuplicateCount() + 1);
						break;
					case BulkImportRowResult.STATUS_VALIDATION_ERROR:
						report.setValidationErrorCount(report.getValidationErrorCount() + 1);
						break;
					case BulkImportRowResult.STATUS_SYSTEM_ERROR:
						report.setSystemErrorCount(report.getSystemErrorCount() + 1);
						break;
				}
			}

			report.setTotalRows(rowNumber - 2); // Exclude header and adjust for 0-based
			report.calculateOverallStatus();
			report.generateSummary();

			// Audit log the import
			auditLogService.log(
					null,
					"BulkImport",
					null,
					com.fitness.entity.AuditLog.Action.CREATE,
					"fileName=" + file.getOriginalFilename(),
					"importResult=" + report.getOverallStatus() + "," + report.getSuccessCount() + "/"
							+ report.getTotalRows());

		} catch (Exception e) {
			BulkImportRowResult errorRow = BulkImportRowResult.builder()
					.rowNumber(0)
					.status(BulkImportRowResult.STATUS_SYSTEM_ERROR)
					.errorMessage("CSV parsing error: " + e.getMessage())
					.build();
			report.getRowResults().add(errorRow);
			report.setSystemErrorCount(1);
			report.setOverallStatus("FAILED");
			report.setSummary("CSV file could not be parsed: " + e.getMessage());
		}

		return report;
	}

	/**
	 * Process a single CSV row and attempt member creation
	 * 
	 * @param record    CSV record
	 * @param rowNumber Row number (for reporting)
	 * @return BulkImportRowResult with status and details
	 */
	private BulkImportRowResult processRow(CSVRecord record, int rowNumber) {
		BulkImportRowResult result = BulkImportRowResult.builder()
				.rowNumber(rowNumber)
				.build();

		try {
			// Extract fields
			String memName = getField(record, "memName");
			String email = getField(record, "email");
			String phone = getField(record, "phone");
			String dobString = getField(record, "dob");
			String address = getField(record, "address");
			String emgContact = getField(record, "emgContact");
			String emgPhone = getField(record, "emgPhone");
			String homeBranchIdStr = getField(record, "homeBranchId");
			String referralCode = getField(record, "referralCode");
			String corporateCode = getField(record, "corporateCode");
			String notes = getField(record, "notes");

			// Validate required fields
			List<String> validationErrors = new ArrayList<>();

			if (memName == null || memName.trim().isEmpty()) {
				validationErrors.add("memName is required");
			}
			if (email == null || email.trim().isEmpty()) {
				validationErrors.add("email is required");
			} else if (!isValidEmail(email)) {
				validationErrors.add("email format is invalid");
			}
			if (phone == null || phone.trim().isEmpty()) {
				validationErrors.add("phone is required");
			} else if (!PHONE_PATTERN.matcher(phone).matches()) {
				validationErrors.add("phone must be valid Indian format (10 digits, starting with 6-9)");
			}
			if (dobString == null || dobString.trim().isEmpty()) {
				validationErrors.add("dob is required");
			}
			if (address == null || address.trim().isEmpty()) {
				validationErrors.add("address is required");
			}
			if (emgContact == null || emgContact.trim().isEmpty()) {
				validationErrors.add("emgContact is required");
			}
			if (emgPhone == null || emgPhone.trim().isEmpty()) {
				validationErrors.add("emgPhone is required");
			} else if (!PHONE_PATTERN.matcher(emgPhone).matches()) {
				validationErrors.add("emgPhone must be valid Indian format");
			}

			if (!validationErrors.isEmpty()) {
				result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
				result.setFieldErrors(String.join("; ", validationErrors));
				result.setEmail(email);
				result.setPhone(phone);
				result.setMemberName(memName);
				return result;
			}

			// Check for duplicates
			if (memberRepository.existsByEmail(email)) {
				result.setStatus(BulkImportRowResult.STATUS_DUPLICATE);
				result.setErrorMessage("Email already exists in system");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			if (memberRepository.existsByPhone(phone)) {
				result.setStatus(BulkImportRowResult.STATUS_DUPLICATE);
				result.setErrorMessage("Phone already exists in system");
				result.setEmail(email);
				result.setPhone(phone);
				result.setMemberName(memName);
				return result;
			}

			if (systemUserRepository.existsByEmail(email)) {
				result.setStatus(BulkImportRowResult.STATUS_DUPLICATE);
				result.setErrorMessage("Email already exists as system user");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			// Validate and parse date
			LocalDate dob;
			try {
				dob = LocalDate.parse(dobString, DATE_FORMATTER);
			} catch (Exception e) {
				result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
				result.setErrorMessage("dob must be in yyyy-MM-dd format");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			// Validate branch
			Long branchId;
			try {
				branchId = Long.parseLong(homeBranchIdStr);
			} catch (NumberFormatException e) {
				result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
				result.setErrorMessage("homeBranchId must be a valid number");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			Optional<Branch> branch = branchRepository.findById(branchId);
			if (branch.isEmpty()) {
				result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
				result.setErrorMessage("homeBranchId " + branchId + " not found");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			// Create MemberDTO
			MemberDTO memberDTO = MemberDTO.builder()
					.memName(memName)
					.email(email)
					.phone(phone)
					.dob(dob.format(DATE_FORMATTER))
					.address(address)
					.emgContact(emgContact)
					.emgPhone(emgPhone)
					.homeBranchId(branchId)
					.referralCode(referralCode)
					.corporateCode(corporateCode)
					.notes(notes)
					.build();

			// Create member
			MemberDTO createdMember = memberService.createMember(memberDTO);

			result.setStatus(BulkImportRowResult.STATUS_SUCCESS);
			result.setMemberId(createdMember.getMemberId());
			result.setEmail(email);
			result.setPhone(phone);
			result.setMemberName(memName);

		} catch (DuplicateResourceException e) {
			result.setStatus(BulkImportRowResult.STATUS_DUPLICATE);
			result.setErrorMessage(e.getMessage());
		} catch (Exception e) {
			result.setStatus(BulkImportRowResult.STATUS_SYSTEM_ERROR);
			result.setErrorMessage("Unexpected error: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Get field value from CSV record, handling missing fields
	 * 
	 * @param record    CSV record
	 * @param fieldName Field name
	 * @return Field value or null if missing
	 */
	private String getField(CSVRecord record, String fieldName) {
		try {
			if (record.isMapped(fieldName)) {
				String value = record.get(fieldName);
				return (value == null || value.trim().isEmpty()) ? null : value.trim();
			}
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Validate email format
	 * 
	 * @param email Email to validate
	 * @return True if valid
	 */
	private boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		return Pattern.compile(emailRegex).matcher(email).matches();
	}
}
