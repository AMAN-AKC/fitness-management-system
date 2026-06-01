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

import com.fitness.entity.Plan;
import com.fitness.entity.Classes;
import com.fitness.entity.Trainer;
import com.fitness.entity.Facility;
import com.fitness.entity.ClassSession;
import com.fitness.repository.PlanRepository;
import com.fitness.repository.ClassesRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.FacilityRepository;
import com.fitness.repository.ClassSessionRepository;
import java.math.BigDecimal;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
public class CsvImportService {

	private final MemberService memberService;
	private final BranchRepository branchRepository;
	private final MemberRepository memberRepository;
	private final SystemUserRepository systemUserRepository;
	private final AuditLogService auditLogService;

	private final PlanRepository planRepository;
	private final ClassesRepository classesRepository;
	private final TrainerRepository trainerRepository;
	private final FacilityRepository facilityRepository;
	private final ClassSessionRepository classSessionRepository;


	// Validation patterns
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	/**
	 * Parse and validate CSV file for bulk member import (AC10)
	 * 
	 * @param file CSV file to import
	 * @return BulkImportReport with per-row results and summary
	 */
	public BulkImportReport importMembers(MultipartFile file, boolean dryRun) {
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
				BulkImportRowResult rowResult = processRow(record, rowNumber, dryRun);
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

			report.setTotalRows(rowNumber - 1); // Exclude header and adjust for 0-based
			report.calculateOverallStatus();
			report.generateSummary();

			// Audit log the import
			auditLogService.logForCurrentUser(
					"BulkImport",
					0L,
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
	private BulkImportRowResult processRow(CSVRecord record, int rowNumber, boolean dryRun) {
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
				result.setErrorMessage("dob must be in dd-MM-yyyy format");
				result.setEmail(email);
				result.setMemberName(memName);
				return result;
			}

			Long branchId = null;
			if (homeBranchIdStr != null && !homeBranchIdStr.trim().isEmpty()) {
				try {
					branchId = Long.parseLong(homeBranchIdStr.trim());
					Optional<Branch> branch = branchRepository.findById(branchId);
					if (branch.isEmpty()) {
						result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
						result.setErrorMessage("homeBranchId " + branchId + " not found");
						result.setEmail(email);
						result.setMemberName(memName);
						return result;
					}
				} catch (NumberFormatException e) {
					result.setStatus(BulkImportRowResult.STATUS_VALIDATION_ERROR);
					result.setErrorMessage("homeBranchId must be a valid number");
					result.setEmail(email);
					result.setMemberName(memName);
					return result;
				}
			}

			// Create MemberDTO
			MemberDTO memberDTO = MemberDTO.builder()
					.memName(memName)
					.email(email)
					.phone(phone)
					.dob(dob.format(DateTimeFormatter.ISO_LOCAL_DATE))
					.address(address)
					.emgContact(emgContact)
					.emgPhone(emgPhone)
					.homeBranchId(branchId)
					.referralCode(referralCode)
					.corporateCode(corporateCode)
					.notes(notes)
					.build();

			// Create member if not dry run
			if (!dryRun) {
				MemberDTO createdMember = memberService.createMember(memberDTO);
				result.setMemberId(createdMember.getMemberId());
			} else {
				result.setMemberId(9999L); // Dummy ID for dry run
			}

			result.setStatus(BulkImportRowResult.STATUS_SUCCESS);
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

	@Transactional
	public BulkImportReport importPlans(MultipartFile file, boolean dryRun) {
		BulkImportReport report = BulkImportReport.builder()
				.fileName(file.getOriginalFilename())
				.processedAt(LocalDateTime.now())
				.rowResults(new ArrayList<>())
				.build();

		int successCount = 0;
		int duplicateCount = 0;
		int validationErrorCount = 0;
		int systemErrorCount = 0;
		int rowNum = 1;

		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			 CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

			for (CSVRecord csvRecord : csvParser) {
				rowNum++;
				BulkImportRowResult rowResult = new BulkImportRowResult();
				rowResult.setRowNumber(rowNum);

				try {
					String planName = csvRecord.get("planName");
					String durationStr = csvRecord.get("durationDays");
					String priceStr = csvRecord.get("price");
					String accessStartStr = csvRecord.get("accessStart");
					String accessEndStr = csvRecord.get("accessEnd");
					String eligibilityStr = csvRecord.get("eligibilityType");
					String prorationRule = csvRecord.get("prorationRule");
					String taxPercentStr = csvRecord.get("taxPercent");
					String branchVisibility = csvRecord.get("branchVisibility");

					if (planName == null || planName.isEmpty()) {
						throw new IllegalArgumentException("planName is required");
					}

					if (planRepository.existsByPlanName(planName)) {
						rowResult.setStatus("FAILED");
						rowResult.setFieldErrors("planName");
						rowResult.setErrorMessage("Plan name already exists");
						duplicateCount++;
						report.getRowResults().add(rowResult);
						continue;
					}

					Integer durationDays = Integer.parseInt(durationStr);
					if (durationDays <= 0) throw new IllegalArgumentException("durationDays must be > 0");

					BigDecimal price = new BigDecimal(priceStr);
					if (price.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("price must be > 0");

					LocalTime accessStart = LocalTime.parse(accessStartStr);
					LocalTime accessEnd = LocalTime.parse(accessEndStr);

					Plan.EligibilityType eligibilityType = Plan.EligibilityType.valueOf(eligibilityStr);
					BigDecimal taxPercent = taxPercentStr != null && !taxPercentStr.isEmpty() ? new BigDecimal(taxPercentStr) : BigDecimal.ZERO;

					if (!dryRun) {
						Plan plan = Plan.builder()
								.planName(planName)
								.durationDays(durationDays)
								.price(price)
								.accessStart(accessStart)
								.accessEnd(accessEnd)
								.eligibilityType(eligibilityType)
								.prorationRule(prorationRule)
								.taxPercent(taxPercent)
								.branchVisibility(branchVisibility)
								.effectiveFrom(LocalDate.now())
								.isActive(true)
								.build();
						planRepository.save(plan);
					}

					rowResult.setStatus("SUCCESS");
					successCount++;

				} catch (Exception e) {
					rowResult.setStatus("FAILED");
					rowResult.setErrorMessage(e.getMessage());
					validationErrorCount++;
				}
				report.getRowResults().add(rowResult);
			}
		} catch (Exception e) {
			systemErrorCount++;
		}

		report.setTotalRows(rowNum - 1);
		report.setSuccessCount(successCount);
		report.setDuplicateCount(duplicateCount);
		report.setValidationErrorCount(validationErrorCount);
		report.setSystemErrorCount(systemErrorCount);
		report.setOverallStatus(validationErrorCount == 0 && systemErrorCount == 0 ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED"));
		report.setSummary("Import processed: " + successCount + "/" + (rowNum - 1) + " success");
		return report;
	}

	@Transactional
	public BulkImportReport importClasses(MultipartFile file, boolean dryRun) {
		BulkImportReport report = BulkImportReport.builder()
				.fileName(file.getOriginalFilename())
				.processedAt(LocalDateTime.now())
				.rowResults(new ArrayList<>())
				.build();

		int successCount = 0;
		int validationErrorCount = 0;
		int systemErrorCount = 0;
		int rowNum = 1;

		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			 CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

			for (CSVRecord csvRecord : csvParser) {
				rowNum++;
				BulkImportRowResult rowResult = new BulkImportRowResult();
				rowResult.setRowNumber(rowNum);

				try {
					String className = csvRecord.get("className");
					Long trainerId = Long.parseLong(csvRecord.get("trainerId"));
					Long roomId = Long.parseLong(csvRecord.get("roomId"));
					Long branchId = Long.parseLong(csvRecord.get("branchId"));
					LocalDate startDate = LocalDate.parse(csvRecord.get("startDate"));
					LocalDate endDate = LocalDate.parse(csvRecord.get("endDate"));
					String weekdays = csvRecord.get("weekdays");
					LocalTime classTime = LocalTime.parse(csvRecord.get("classTime"));
					Integer durationMins = Integer.parseInt(csvRecord.get("durationMins"));
					Integer capacity = Integer.parseInt(csvRecord.get("capacity"));
					String prerequisites = csvRecord.get("prerequisites");
					String planEligibility = csvRecord.get("planEligibility");

					Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
					Facility room = facilityRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
					Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new IllegalArgumentException("Branch not found"));

					if (!dryRun) {
						Classes cls = Classes.builder()
								.className(className)
								.trainer(trainer)
								.room(room)
								.branch(branch)
								.startDate(startDate)
								.endDate(endDate)
								.weekdays(weekdays)
								.classTime(classTime)
								.durationMins(durationMins)
								.capacity(capacity)
								.prerequisites(prerequisites)
								.planEligibility(planEligibility)
								.status(Classes.Status.ACTIVE)
								.build();
						classesRepository.save(cls);
					}
					rowResult.setStatus("SUCCESS");
					successCount++;
				} catch (Exception e) {
					rowResult.setStatus("FAILED");
					rowResult.setErrorMessage(e.getMessage());
					validationErrorCount++;
				}
				report.getRowResults().add(rowResult);
			}
		} catch (Exception e) {
			systemErrorCount++;
		}

		report.setTotalRows(rowNum - 1);
		report.setSuccessCount(successCount);
		report.setValidationErrorCount(validationErrorCount);
		report.setSystemErrorCount(systemErrorCount);
		report.setOverallStatus(validationErrorCount == 0 && systemErrorCount == 0 ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED"));
		report.setSummary("Import processed: " + successCount + "/" + (rowNum - 1) + " success");
		return report;
	}

	@Transactional
	public BulkImportReport importSchedule(MultipartFile file, boolean dryRun) {
		BulkImportReport report = BulkImportReport.builder()
				.fileName(file.getOriginalFilename())
				.processedAt(LocalDateTime.now())
				.rowResults(new ArrayList<>())
				.build();

		int successCount = 0;
		int validationErrorCount = 0;
		int systemErrorCount = 0;
		int rowNum = 1;

		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
			 CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

			for (CSVRecord csvRecord : csvParser) {
				rowNum++;
				BulkImportRowResult rowResult = new BulkImportRowResult();
				rowResult.setRowNumber(rowNum);

				try {
					Long classId = Long.parseLong(csvRecord.get("classId"));
					Long trainerId = Long.parseLong(csvRecord.get("trainerId"));
					Long roomId = Long.parseLong(csvRecord.get("roomId"));
					LocalDate date = LocalDate.parse(csvRecord.get("date"));
					LocalTime time = LocalTime.parse(csvRecord.get("time"));
					String statusStr = csvRecord.get("status");

					Classes cls = classesRepository.findById(classId).orElseThrow(() -> new IllegalArgumentException("Class not found"));
					Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
					Facility room = facilityRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));

					ClassSession.SessionStatus status = ClassSession.SessionStatus.valueOf(statusStr);

					if (!dryRun) {
						ClassSession session = ClassSession.builder()
								.fitnessClass(cls)
								.trainer(trainer)
								.room(room)
								.sessionDate(date)
								.sessionTime(time)
								.status(status)
								.build();
						classSessionRepository.save(session);
					}
					rowResult.setStatus("SUCCESS");
					successCount++;
				} catch (Exception e) {
					rowResult.setStatus("FAILED");
					rowResult.setErrorMessage(e.getMessage());
					validationErrorCount++;
				}
				report.getRowResults().add(rowResult);
			}
		} catch (Exception e) {
			systemErrorCount++;
		}

		report.setTotalRows(rowNum - 1);
		report.setSuccessCount(successCount);
		report.setValidationErrorCount(validationErrorCount);
		report.setSystemErrorCount(systemErrorCount);
		report.setOverallStatus(validationErrorCount == 0 && systemErrorCount == 0 ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED"));
		report.setSummary("Import processed: " + successCount + "/" + (rowNum - 1) + " success");
		return report;
	}

}