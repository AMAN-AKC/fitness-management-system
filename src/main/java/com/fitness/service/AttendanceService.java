package com.fitness.service;

import com.fitness.dto.AttendanceDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepo;
	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final MembershipRepository membershipRepo;
	private final ClassesRepository classesRepo;
	private final InvoiceRepository invoiceRepo;
	private final SystemUserRepository userRepo;
	private final AuditLogService auditLogService;
	private final HealthConsentService healthConsentService;
	private final ModelMapper mapper;

	@Value("${attendance.duplicate-check-minutes:30}")
	private int duplicateCheckMinutes;

	@Value("${attendance.class-window-minutes:15}")
	private int classWindowMinutes;

	/**
	 * AC01/AC02/AC03/AC04/AC05/AC10: Check-in with full validation.
	 */
	public AttendanceDTO checkIn(AttendanceDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		// AC02: Verify active membership — block expired/suspended
		Optional<Membership> activeMembership = membershipRepo
				.findByMemberMemberIdAndStatus(member.getMemberId(), Membership.Status.ACTIVE);
		if (activeMembership.isEmpty())
			throw new BusinessRuleException("Member does not have an active membership. Check-in denied.");

		// AC04: Duplicate check-in guard (configurable timeframe)
		LocalDateTime from = LocalDateTime.now().minusMinutes(duplicateCheckMinutes);
		boolean alreadyCheckedIn = attendanceRepo
				.existsByMemberMemberIdAndCheckInTimeBetween(member.getMemberId(), from, LocalDateTime.now());
		if (alreadyCheckedIn)
			throw new BusinessRuleException(
					"Duplicate check-in detected within the last " + duplicateCheckMinutes + " minutes.");

		// AC05: Build alert flags
		boolean alertFlag = false;
		StringBuilder alertNotes = new StringBuilder();

		// Check unpaid dues
		List<Invoice> overdueInvoices = invoiceRepo.findByStatus(Invoice.Status.OVERDUE).stream()
				.filter(inv -> inv.getMember().getMemberId().equals(member.getMemberId()))
				.collect(Collectors.toList());
		if (!overdueInvoices.isEmpty()) {
			BigDecimal totalDues = overdueInvoices.stream()
					.map(Invoice::getOutstanding)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			alertFlag = true;
			alertNotes.append("UNPAID DUES: ₹").append(totalDues).append(". ");
		}

		// Check member status warnings
		if (member.getStatus() == Member.Status.SUSPENDED) {
			throw new BusinessRuleException("Member account is suspended. Check-in denied.");
		}
		if (member.getStatus() == Member.Status.DEACTIVATED) {
			throw new BusinessRuleException("Member account is deactivated. Check-in denied.");
		}

		Map<String, Object> consentStatus = healthConsentService.getConsentStatus(member.getMemberId());
		if (Boolean.TRUE.equals(consentStatus.get("consentRequired"))) {
			alertFlag = true;
			alertNotes.append("CONSENT REQUIRED: Health waiver is missing, expired, or policy version changed. ");
		}

		Attendance attendance = Attendance.builder()
				.member(member)
				.branch(branch)
				.checkInTime(LocalDateTime.now())
				.scanMethod(dto.getScanMethod())
				.alertFlag(alertFlag)
				.syncStatus(Attendance.SyncStatus.SYNCED)
				.build();

		// AC03: Auto-link class attendance if check-in is within class window
		if (dto.getClassId() != null) {
			Classes cls = classesRepo.findById(dto.getClassId())
					.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
			attendance.setFitnessClass(cls);
		} else {
			// Auto-detect: find class starting within window at this branch
			LocalTime now = LocalTime.now();
			LocalTime windowStart = now.minusMinutes(classWindowMinutes);
			LocalTime windowEnd = now.plusMinutes(classWindowMinutes);
			classesRepo.findByBranchBranchId(branch.getBranchId()).stream()
					.filter(c -> c.getStatus() == Classes.Status.ACTIVE)
					.filter(c -> {
						LocalTime ct = c.getClassTime();
						return !ct.isBefore(windowStart) && !ct.isAfter(windowEnd);
					})
					.findFirst()
					.ifPresent(attendance::setFitnessClass);
		}

		Attendance saved = attendanceRepo.save(attendance);

		// AC10: Audit log
		String details = "Check-in: member=" + member.getMemName()
				+ " method=" + dto.getScanMethod()
				+ " branch=" + branch.getBranchName()
				+ (saved.getFitnessClass() != null ? " class=" + saved.getFitnessClass().getClassName() : "")
				+ (alertFlag ? " ALERTS: " + alertNotes : "");
		auditLogService.logForCurrentUser("Attendance", saved.getLogId(), AuditLog.Action.CREATE,
				null, details);

		AttendanceDTO result = mapper.map(saved, AttendanceDTO.class);
		result.setAlertFlag(alertFlag);
		return result;
	}

	/**
	 * AC09/AC10: Staff override check-in with reason (bypasses membership check).
	 */
	public AttendanceDTO overrideCheckIn(AttendanceDTO dto, Long overrideByUserId, String reason) {
		if (reason == null || reason.isBlank())
			throw new BusinessRuleException("Override reason is required.");

		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
		SystemUser overrideUser = userRepo.findById(overrideByUserId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", overrideByUserId));

		Attendance attendance = Attendance.builder()
				.member(member)
				.branch(branch)
				.checkInTime(LocalDateTime.now())
				.scanMethod(dto.getScanMethod() != null ? dto.getScanMethod() : Attendance.ScanMethod.MANUAL)
				.alertFlag(false)
				.syncStatus(Attendance.SyncStatus.SYNCED)
				.overrideBy(overrideUser)
				.overrideReason(reason)
				.build();

		if (dto.getClassId() != null) {
			Classes cls = classesRepo.findById(dto.getClassId())
					.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
			attendance.setFitnessClass(cls);
		}

		Attendance saved = attendanceRepo.save(attendance);

		// AC10: Audit log
		auditLogService.logForCurrentUser("Attendance", saved.getLogId(), AuditLog.Action.CREATE,
				null, "OVERRIDE check-in by " + overrideUser.getUsername()
						+ ": " + reason + " | member=" + member.getMemName());

		return mapper.map(saved, AttendanceDTO.class);
	}

	/**
	 * AC08: Trainer marks attendance from class roster.
	 */
	public AttendanceDTO markClassAttendance(Long memberId, Long classId, Long branchId) {
		Member member = memberRepo.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));
		Branch branch = branchRepo.findById(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
		Classes cls = classesRepo.findById(classId)
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

		Attendance attendance = Attendance.builder()
				.member(member)
				.branch(branch)
				.fitnessClass(cls)
				.checkInTime(LocalDateTime.now())
				.scanMethod(Attendance.ScanMethod.MANUAL)
				.alertFlag(false)
				.syncStatus(Attendance.SyncStatus.SYNCED)
				.build();

		Attendance saved = attendanceRepo.save(attendance);

		auditLogService.logForCurrentUser("Attendance", saved.getLogId(), AuditLog.Action.CREATE,
				null, "Trainer marked attendance: member=" + member.getMemName()
						+ " class=" + cls.getClassName());

		return mapper.map(saved, AttendanceDTO.class);
	}

	/**
	 * AC06: Queue check-in for offline sync (stub — saves with PENDING status).
	 */
	public AttendanceDTO queueOfflineCheckIn(AttendanceDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		Attendance attendance = Attendance.builder()
				.member(member)
				.branch(branch)
				.checkInTime(LocalDateTime.now())
				.scanMethod(dto.getScanMethod() != null ? dto.getScanMethod() : Attendance.ScanMethod.MANUAL)
				.alertFlag(false)
				.syncStatus(Attendance.SyncStatus.PENDING)
				.build();

		Attendance saved = attendanceRepo.save(attendance);
		return mapper.map(saved, AttendanceDTO.class);
	}

	/**
	 * AC06: Sync all pending check-ins (batch process).
	 */
	public List<AttendanceDTO> syncPendingCheckIns() {
		List<Attendance> pending = attendanceRepo.findBySyncStatus(Attendance.SyncStatus.PENDING);
		for (Attendance a : pending) {
			a.setSyncStatus(Attendance.SyncStatus.SYNCED);
			attendanceRepo.save(a);
		}
		return pending.stream().map(a -> mapper.map(a, AttendanceDTO.class)).collect(Collectors.toList());
	}

	/**
	 * AC05: Get check-in flags for a member (dues, health notes).
	 */
	public Map<String, Object> getMemberCheckInFlags(Long memberId) {
		Member member = memberRepo.findById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

		Map<String, Object> flags = new LinkedHashMap<>();
		flags.put("memberId", memberId);
		flags.put("memberName", member.getMemName());
		flags.put("status", member.getStatus().name());

		// Check unpaid dues
		List<Invoice> overdueInvoices = invoiceRepo.findByStatus(Invoice.Status.OVERDUE).stream()
				.filter(inv -> inv.getMember().getMemberId().equals(memberId))
				.collect(Collectors.toList());
		BigDecimal totalDues = overdueInvoices.stream()
				.map(Invoice::getOutstanding)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		flags.put("unpaidDues", totalDues);
		flags.put("hasUnpaidDues", totalDues.compareTo(BigDecimal.ZERO) > 0);

		// Check active membership
		Optional<Membership> active = membershipRepo
				.findByMemberMemberIdAndStatus(memberId, Membership.Status.ACTIVE);
		flags.put("hasActiveMembership", active.isPresent());
		flags.put("membershipStatus", active.map(m -> m.getStatus().name()).orElse("NONE"));

		// Notes (health alerts)
		flags.put("notes", member.getNotes());
		flags.put("hasHealthNotes", member.getNotes() != null && !member.getNotes().isBlank());

		Map<String, Object> consentStatus = healthConsentService.getConsentStatus(memberId);
		flags.put("consentRequired", consentStatus.get("consentRequired"));
		flags.put("requiresReconfirmation", consentStatus.get("requiresReconfirmation"));
		flags.put("consentCurrentVersion", consentStatus.get("currentVersion"));

		return flags;
	}

	public List<AttendanceDTO> getAttendanceByMember(Long memberId) {
		return attendanceRepo.findByMemberMemberId(memberId).stream()
				.map(a -> mapper.map(a, AttendanceDTO.class)).collect(Collectors.toList());
	}

	public List<AttendanceDTO> getTodayAttendanceByBranch(Long branchId) {
		LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
		return attendanceRepo.findByCheckInTimeBetween(startOfDay, LocalDateTime.now()).stream()
				.filter(a -> a.getBranch().getBranchId().equals(branchId))
				.map(a -> mapper.map(a, AttendanceDTO.class)).collect(Collectors.toList());
	}

	/**
	 * AC07: Export daily attendance report as CSV bytes.
	 */
	public byte[] exportDailyAttendanceCsv(Long branchId) {
		LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
		List<Attendance> records = attendanceRepo.findByCheckInTimeBetween(startOfDay, LocalDateTime.now())
				.stream()
				.filter(a -> a.getBranch().getBranchId().equals(branchId))
				.collect(Collectors.toList());

		try (StringWriter sw = new StringWriter();
				CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
						.setHeader("logId", "memberName", "memberId", "branchName", "checkInTime",
								"checkOutTime", "scanMethod", "className", "alertFlag",
								"overrideBy", "overrideReason", "syncStatus")
						.build())) {
			for (Attendance a : records) {
				printer.printRecord(
						a.getLogId(),
						a.getMember().getMemName(),
						a.getMember().getMemberId(),
						a.getBranch().getBranchName(),
						a.getCheckInTime(),
						a.getCheckOutTime(),
						a.getScanMethod(),
						a.getFitnessClass() != null ? a.getFitnessClass().getClassName() : "",
						a.getAlertFlag(),
						a.getOverrideBy() != null ? a.getOverrideBy().getUsername() : "",
						a.getOverrideReason() != null ? a.getOverrideReason() : "",
						a.getSyncStatus());
			}
			printer.flush();
			return sw.toString().getBytes(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new BusinessRuleException("Failed to export attendance CSV: " + e.getMessage());
		}
	}
}
