package com.fitness.service;

import com.fitness.dto.ClassesDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassesService {

	private final ClassesRepository classesRepo;
	private final TrainerRepository trainerRepo;
	private final FacilityRepository facilityRepo;
	private final BranchRepository branchRepo;
	private final ClassBookingRepository bookingRepo;
	private final AuditLogService auditLogService;
	private final NotificationService notificationService;
	private final ModelMapper mapper;

	/**
	 * AC01/AC02/AC03/AC07/AC10: Create class with conflict detection, maintenance
	 * check, and audit logging.
	 */
	@Transactional
	public ClassesDTO createClass(ClassesDTO dto) {
		Trainer trainer = trainerRepo.findById(dto.getTrainerId())
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", dto.getTrainerId()));
		Facility room = facilityRepo.findById(dto.getRoomId())
				.orElseThrow(() -> new ResourceNotFoundException("Facility", "id", dto.getRoomId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		// AC07: Block scheduling in rooms under maintenance
		if (Boolean.TRUE.equals(room.getUnderMaintenance())) {
			throw new BusinessRuleException("Room '" + room.getFacilityName()
					+ "' is under maintenance: " + (room.getMaintenanceReason() != null ? room.getMaintenanceReason() : ""));
		}

		// AC03: Conflict detection
		LocalTime start = LocalTime.parse(dto.getClassTime());
		LocalTime end = start.plusMinutes(dto.getDurationMins());

		if (!classesRepo.findConflictingByRoom(room.getFacilityId(), start, end).isEmpty())
			throw new BusinessRuleException("Room is already booked at this time slot.");
		if (!classesRepo.findConflictingByTrainer(trainer.getTrainerId(), start, end).isEmpty())
			throw new BusinessRuleException("Trainer is already assigned to another class at this time.");

		Classes cls = mapper.map(dto, Classes.class);
		cls.setTrainer(trainer);
		cls.setRoom(room);
		cls.setBranch(branch);
		cls.setStatus(Classes.Status.ACTIVE);
		Classes saved = classesRepo.save(cls);

		// AC10: Audit log
		auditLogService.logForCurrentUser("Class", saved.getClassId(), AuditLog.Action.CREATE,
				null, "Class created: " + saved.getClassName() + " | Trainer: " + trainer.getTrainerId()
						+ " | Room: " + room.getFacilityName());

		return mapper.map(saved, ClassesDTO.class);
	}

	public List<ClassesDTO> getAllClasses() {
		return classesRepo.findAll().stream().map(c -> mapper.map(c, ClassesDTO.class)).collect(Collectors.toList());
	}

	public List<ClassesDTO> getClassesByBranch(Long branchId) {
		return classesRepo.findByBranchBranchId(branchId).stream().map(c -> mapper.map(c, ClassesDTO.class))
				.collect(Collectors.toList());
	}

	public ClassesDTO getClassById(Long id) {
		return mapper.map(findById(id), ClassesDTO.class);
	}

	/**
	 * AC10: Update class with audit logging.
	 */
	@Transactional
	public ClassesDTO updateClass(Long id, ClassesDTO dto) {
		Classes cls = findById(id);
		String oldState = cls.getClassName() + " | Trainer:" + cls.getTrainer().getTrainerId();
		mapper.map(dto, cls);
		Classes saved = classesRepo.save(cls);

		auditLogService.logForCurrentUser("Class", id, AuditLog.Action.UPDATE,
				oldState, "Class updated: " + saved.getClassName());

		return mapper.map(saved, ClassesDTO.class);
	}

	/**
	 * AC06/AC10: Cancel class with reason, notify booked members, audit log.
	 */
	@Transactional
	public void cancelClass(Long id, String reason) {
		Classes cls = findById(id);
		if (reason == null || reason.isBlank())
			throw new BusinessRuleException("Cancel reason is required.");
		cls.setStatus(Classes.Status.CANCELLED);
		cls.setCancelReason(reason);
		classesRepo.save(cls);

		// AC10: Audit log
		auditLogService.logForCurrentUser("Class", id, AuditLog.Action.UPDATE,
				"status=ACTIVE", "status=CANCELLED | reason=" + reason);

		// AC06: Notify all booked members
		notifyBookedMembers(cls, Notification.NotifType.CANCELLATION,
				"Class Cancelled: " + cls.getClassName(),
				"Your class '" + cls.getClassName() + "' has been cancelled. Reason: " + reason);
	}

	/**
	 * AC05: Substitute trainer with conflict check and member notification.
	 */
	@Transactional
	public ClassesDTO substituteTrainer(Long classId, Long newTrainerId, String reason) {
		Classes cls = findById(classId);
		Trainer newTrainer = trainerRepo.findById(newTrainerId)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", newTrainerId));

		// Check conflict for new trainer
		LocalTime start = cls.getClassTime();
		LocalTime end = start.plusMinutes(cls.getDurationMins());
		List<Classes> conflicts = classesRepo.findConflictingByTrainer(newTrainerId, start, end);
		// Exclude current class from conflicts
		conflicts = conflicts.stream().filter(c -> !c.getClassId().equals(classId)).collect(Collectors.toList());
		if (!conflicts.isEmpty())
			throw new BusinessRuleException("New trainer has a scheduling conflict at this time.");

		String oldTrainerName = cls.getTrainer().getTrainerId().toString();
		cls.setTrainer(newTrainer);
		Classes saved = classesRepo.save(cls);

		// AC10: Audit log
		auditLogService.logForCurrentUser("Class", classId, AuditLog.Action.UPDATE,
				"trainerId=" + oldTrainerName,
				"trainerId=" + newTrainerId + " | reason=" + reason);

		// AC05: Notify booked members
		notifyBookedMembers(cls, Notification.NotifType.SCHEDULE_CHANGE,
				"Trainer Change: " + cls.getClassName(),
				"The trainer for '" + cls.getClassName() + "' has been changed. Reason: " + reason);

		return mapper.map(saved, ClassesDTO.class);
	}

	/**
	 * AC09: Export all classes as CSV bytes.
	 */
	public byte[] exportClassesAsCsv() {
		List<Classes> classes = classesRepo.findAll();
		try (StringWriter sw = new StringWriter();
				CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
						.setHeader("classId", "className", "trainerId", "roomId", "branchId",
								"startDate", "endDate", "weekdays", "classTime", "durationMins",
								"capacity", "prerequisites", "planEligibility", "status", "cancelReason")
						.build())) {
			for (Classes c : classes) {
				printer.printRecord(
						c.getClassId(), c.getClassName(),
						c.getTrainer().getTrainerId(), c.getRoom().getFacilityId(),
						c.getBranch().getBranchId(),
						c.getStartDate(), c.getEndDate(), c.getWeekdays(),
						c.getClassTime(), c.getDurationMins(), c.getCapacity(),
						c.getPrerequisites(), c.getPlanEligibility(),
						c.getStatus(), c.getCancelReason());
			}
			printer.flush();
			return sw.toString().getBytes(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new BusinessRuleException("Failed to export classes as CSV: " + e.getMessage());
		}
	}

	/**
	 * AC09: Import classes from CSV with row-level validation.
	 */
	@Transactional
	public List<Map<String, Object>> importClassesFromCsv(MultipartFile file) {
		List<Map<String, Object>> results = new ArrayList<>();
		try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
				CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
						.parse(reader)) {
			int row = 0;
			for (CSVRecord record : parser) {
				row++;
				Map<String, Object> result = new HashMap<>();
				result.put("row", row);
				try {
					ClassesDTO dto = ClassesDTO.builder()
							.className(record.get("className"))
							.trainerId(Long.parseLong(record.get("trainerId")))
							.roomId(Long.parseLong(record.get("roomId")))
							.branchId(Long.parseLong(record.get("branchId")))
							.startDate(record.get("startDate"))
							.endDate(record.get("endDate"))
							.weekdays(record.get("weekdays"))
							.classTime(record.get("classTime"))
							.durationMins(Integer.parseInt(record.get("durationMins")))
							.capacity(Integer.parseInt(record.get("capacity")))
							.prerequisites(record.isMapped("prerequisites") ? record.get("prerequisites") : null)
							.planEligibility(record.isMapped("planEligibility") ? record.get("planEligibility") : null)
							.build();
					ClassesDTO created = createClass(dto);
					result.put("status", "SUCCESS");
					result.put("classId", created.getClassId());
				} catch (Exception e) {
					result.put("status", "ERROR");
					result.put("error", e.getMessage());
				}
				results.add(result);
			}
		} catch (IOException e) {
			throw new BusinessRuleException("Failed to parse CSV: " + e.getMessage());
		}
		return results;
	}

	/**
	 * Notify all booked members for a given class.
	 */
	private void notifyBookedMembers(Classes cls, Notification.NotifType type, String title, String body) {
		List<ClassBooking> bookings = bookingRepo.findByFitnessClassClassIdAndBookingStatus(
				cls.getClassId(), ClassBooking.BookingStatus.CONFIRMED);
		for (ClassBooking booking : bookings) {
			try {
				Long userId = booking.getMember().getUser().getUserId();
				notificationService.sendNotification(userId, type,
						Notification.Channel.IN_APP, title, body);
			} catch (Exception ignored) {
				// Best-effort notification
			}
		}
	}

	private Classes findById(Long id) {
		return classesRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
	}
}