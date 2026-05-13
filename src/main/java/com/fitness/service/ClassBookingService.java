package com.fitness.service;

import com.fitness.dto.ClassBookingDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassBookingService {

	private final ClassBookingRepository bookingRepo;
	private final ClassesRepository classesRepo;
	private final MemberRepository memberRepo;
	private final MembershipRepository membershipRepo;
	private final SystemUserRepository userRepo;
	private final AuditLogService auditLogService;
	private final NotificationService notificationService;
	private final ModelMapper mapper;

	private static final int CANCEL_CUTOFF_HOURS = 2;

	/** AC05: Configurable no-show penalty threshold */
	@Value("${booking.noshow.penalty-threshold:3}")
	private int noShowPenaltyThreshold;

	/**
	 * AC01/AC02/AC03/AC06/AC09/AC10: Book a class with full validation.
	 */
	public ClassBookingDTO bookClass(ClassBookingDTO dto) {
		Classes cls = classesRepo.findById(dto.getClassId())
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));

		// AC01: Only active members
		if (member.getStatus() != Member.Status.ACTIVE)
			throw new BusinessRuleException("Only active members can book classes.");
		if (cls.getStatus() != Classes.Status.ACTIVE)
			throw new BusinessRuleException("This class is not available for booking.");

		// AC01: Plan eligibility check
		if (cls.getPlanEligibility() != null && !cls.getPlanEligibility().isBlank()) {
			Optional<Membership> activeMembership = membershipRepo
					.findByMemberMemberIdAndStatus(member.getMemberId(), Membership.Status.ACTIVE);
			if (activeMembership.isEmpty()) {
				throw new BusinessRuleException("You must have an active membership to book this class.");
			}
			String planElig = cls.getPlanEligibility().toUpperCase();
			String memberPlanType = activeMembership.get().getPlan().getEligibilityType().name();
			if (!planElig.contains("ALL") && !planElig.contains(memberPlanType)) {
				throw new BusinessRuleException(
						"Your plan (" + memberPlanType + ") is not eligible for this class. Required: "
								+ cls.getPlanEligibility());
			}
		}

		// AC09: Prevent overlapping time bookings across different classes
		LocalTime classStart = cls.getClassTime();
		LocalTime classEnd = classStart.plusMinutes(cls.getDurationMins());
		List<ClassBooking> memberActiveBookings = bookingRepo.findByMemberMemberId(member.getMemberId())
				.stream()
				.filter(b -> b.getBookingStatus() == ClassBooking.BookingStatus.CONFIRMED
						|| b.getBookingStatus() == ClassBooking.BookingStatus.WAITLISTED)
				.collect(Collectors.toList());

		for (ClassBooking existingBooking : memberActiveBookings) {
			Classes existingClass = existingBooking.getFitnessClass();
			if (existingClass.getClassId().equals(cls.getClassId())) {
				throw new BusinessRuleException("You already have a booking or waitlist spot for this class.");
			}
			// Check time overlap on same date range
			if (datesOverlap(cls, existingClass)) {
				LocalTime existStart = existingClass.getClassTime();
				LocalTime existEnd = existStart.plusMinutes(existingClass.getDurationMins());
				if (classStart.isBefore(existEnd) && classEnd.isAfter(existStart)) {
					throw new BusinessRuleException(
							"Time conflict: You already have '" + existingClass.getClassName()
									+ "' booked at " + existStart + "-" + existEnd);
				}
			}
		}

		// AC02: Capacity + waitlist
		long confirmed = bookingRepo.countByFitnessClassClassIdAndBookingStatus(
				cls.getClassId(), ClassBooking.BookingStatus.CONFIRMED);

		ClassBooking booking = ClassBooking.builder()
				.fitnessClass(cls)
				.member(member)
				.build();

		if (confirmed < cls.getCapacity()) {
			booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
		} else {
			long waitlistCount = bookingRepo.countByFitnessClassClassIdAndBookingStatus(
					cls.getClassId(), ClassBooking.BookingStatus.WAITLISTED);
			booking.setBookingStatus(ClassBooking.BookingStatus.WAITLISTED);
			booking.setWaitlistPosition((int) waitlistCount + 1);
		}

		ClassBooking saved = bookingRepo.save(booking);

		// AC10: Audit log
		auditLogService.logForCurrentUser("ClassBooking", saved.getBookingId(), AuditLog.Action.CREATE,
				null, "Booking created: class=" + cls.getClassName() + " member=" + member.getMemName()
						+ " status=" + saved.getBookingStatus());

		// AC06: Send booking confirmation notification
		try {
			Long userId = member.getUser().getUserId();
			String statusMsg = saved.getBookingStatus() == ClassBooking.BookingStatus.CONFIRMED
					? "Your booking is confirmed!"
					: "You've been added to the waitlist (position " + saved.getWaitlistPosition() + ")";
			notificationService.sendNotification(userId, Notification.NotifType.BOOKING,
					Notification.Channel.IN_APP,
					"Booking: " + cls.getClassName(),
					statusMsg + " Class: " + cls.getClassName() + " at " + cls.getClassTime());
		} catch (Exception ignored) {
			// Best-effort
		}

		return mapper.map(saved, ClassBookingDTO.class);
	}

	/**
	 * AC03/AC04/AC06/AC10: Cancel booking with cutoff, waitlist promotion,
	 * notification, audit.
	 */
	public void cancelBooking(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		// AC04: Cutoff policy
		LocalDateTime classDateTime = booking.getFitnessClass().getStartDate()
				.atTime(booking.getFitnessClass().getClassTime());
		if (LocalDateTime.now().isAfter(classDateTime.minusHours(CANCEL_CUTOFF_HOURS)))
			throw new BusinessRuleException("Cannot cancel within " + CANCEL_CUTOFF_HOURS + " hours of the class.");

		booking.setBookingStatus(ClassBooking.BookingStatus.CANCELLED);
		booking.setCancelledAt(LocalDateTime.now());
		bookingRepo.save(booking);

		// AC10: Audit log
		auditLogService.logForCurrentUser("ClassBooking", bookingId, AuditLog.Action.UPDATE,
				"status=CONFIRMED", "status=CANCELLED");

		// AC06: Send cancellation notification
		try {
			Long userId = booking.getMember().getUser().getUserId();
			notificationService.sendNotification(userId, Notification.NotifType.CANCELLATION,
					Notification.Channel.IN_APP,
					"Booking Cancelled",
					"Your booking for '" + booking.getFitnessClass().getClassName() + "' has been cancelled.");
		} catch (Exception ignored) {
		}

		// AC03: Promote next on waitlist (FIFO)
		bookingRepo.findFirstByFitnessClassClassIdAndBookingStatusOrderByWaitlistPositionAsc(
				booking.getFitnessClass().getClassId(), ClassBooking.BookingStatus.WAITLISTED)
				.ifPresent(next -> {
					next.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
					next.setWaitlistPosition(null);
					bookingRepo.save(next);

					// Notify promoted member
					try {
						Long promoUserId = next.getMember().getUser().getUserId();
						notificationService.sendNotification(promoUserId, Notification.NotifType.BOOKING,
								Notification.Channel.IN_APP,
								"Waitlist Promotion!",
								"You've been promoted from the waitlist for '"
										+ next.getFitnessClass().getClassName() + "'. Your booking is now confirmed!");
					} catch (Exception ignored) {
					}

					auditLogService.logForCurrentUser("ClassBooking", next.getBookingId(), AuditLog.Action.UPDATE,
							"status=WAITLISTED", "status=CONFIRMED (auto-promoted)");
				});
	}

	/**
	 * AC08: Staff override booking rules with justification.
	 */
	public ClassBookingDTO overrideBooking(ClassBookingDTO dto, Long overrideByUserId, String reason) {
		if (reason == null || reason.isBlank())
			throw new BusinessRuleException("Override reason is required.");

		SystemUser overrideUser = userRepo.findById(overrideByUserId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", overrideByUserId));

		Classes cls = classesRepo.findById(dto.getClassId())
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));

		ClassBooking booking = ClassBooking.builder()
				.fitnessClass(cls)
				.member(member)
				.bookingStatus(ClassBooking.BookingStatus.CONFIRMED)
				.overrideBy(overrideUser)
				.overrideReason(reason)
				.build();

		ClassBooking saved = bookingRepo.save(booking);

		auditLogService.logForCurrentUser("ClassBooking", saved.getBookingId(), AuditLog.Action.CREATE,
				null, "OVERRIDE booking by " + overrideUser.getUsername() + ": " + reason);

		return mapper.map(saved, ClassBookingDTO.class);
	}

	/**
	 * AC05: Mark booking as no-show. Applies penalty if threshold exceeded.
	 */
	public ClassBookingDTO markNoShow(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		if (booking.getBookingStatus() != ClassBooking.BookingStatus.CONFIRMED)
			throw new BusinessRuleException("Only confirmed bookings can be marked as no-show.");

		booking.setBookingStatus(ClassBooking.BookingStatus.NO_SHOW);
		bookingRepo.save(booking);

		auditLogService.logForCurrentUser("ClassBooking", bookingId, AuditLog.Action.UPDATE,
				"status=CONFIRMED", "status=NO_SHOW");

		// AC05: Check penalty threshold
		long noShowCount = bookingRepo.findByMemberMemberId(booking.getMember().getMemberId())
				.stream()
				.filter(b -> b.getBookingStatus() == ClassBooking.BookingStatus.NO_SHOW)
				.count();

		if (noShowCount >= noShowPenaltyThreshold) {
			try {
				Long userId = booking.getMember().getUser().getUserId();
				notificationService.sendNotification(userId, Notification.NotifType.GENERAL,
						Notification.Channel.IN_APP,
						"No-Show Warning",
						"You have " + noShowCount + " no-shows. Repeated no-shows may result in booking restrictions.");
			} catch (Exception ignored) {
			}

			auditLogService.logForCurrentUser("Member", booking.getMember().getMemberId(), AuditLog.Action.UPDATE,
					null, "No-show penalty threshold reached: " + noShowCount + " no-shows");
		}

		return mapper.map(booking, ClassBookingDTO.class);
	}

	/**
	 * AC07: Get bookings for a member.
	 */
	public List<ClassBookingDTO> getBookingsByMember(Long memberId) {
		return bookingRepo.findByMemberMemberId(memberId).stream()
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}

	public List<ClassBookingDTO> getBookingsByClass(Long classId) {
		return bookingRepo.findByFitnessClassClassId(classId).stream()
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}

	/**
	 * Check if two classes have overlapping date ranges.
	 */
	private boolean datesOverlap(Classes a, Classes b) {
		return !a.getStartDate().isAfter(b.getEndDate()) && !a.getEndDate().isBefore(b.getStartDate());
	}
}