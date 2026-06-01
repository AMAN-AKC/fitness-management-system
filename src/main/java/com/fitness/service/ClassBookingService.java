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
	private final HealthConsentService healthConsentService;
	private final org.springframework.context.ApplicationEventPublisher eventPublisher;
	private final ModelMapper mapper;

	@Value("${booking.cancel.cutoff-hours:2}")
	private int cancelCutoffHours;

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

		// Health Consent check
		if (!healthConsentService.hasActiveConsent(member.getMemberId())) {
			throw new BusinessRuleException("You must complete your Health Consent and PAR-Q form before booking classes.");
		}

		// AC01: Only active members
		if (member.getStatus() != Member.Status.ACTIVE)
			throw new BusinessRuleException("Only active members can book classes.");
		if (cls.getStatus() != Classes.Status.ACTIVE)
			throw new BusinessRuleException("This class is not available for booking.");

		// Late Cutoff Restriction: All class registration and waitlist movements lock 15 minutes before the class begins
		LocalDateTime classDateTime = getNextOccurrence(cls);
		if (LocalDateTime.now().isAfter(classDateTime.minusMinutes(15))) {
			throw new BusinessRuleException("Class registration is closed (locks 15 minutes before class starts).");
		}

		// Enforce No-Show/Late Cancellation ban: 3 infractions in 30 days results in a 7-day class booking ban
		if (isBookingBanned(member.getMemberId())) {
			throw new BusinessRuleException("You are currently banned from booking classes due to excessive no-shows/late cancellations (3 strikes in 30 days).");
		}

		// Clean up any expired waitlist promotions before determining spots
		checkAndEvictExpiredWaitlistPromotions(cls.getClassId());

		// Plan & Multi-Branch Eligibility Check
		List<Membership> activeMemberships = membershipRepo
				.findByMemberMemberIdAndStatus(member.getMemberId(), Membership.Status.ACTIVE);
		if (activeMemberships.isEmpty()) {
			throw new BusinessRuleException("You must have an active membership to book classes.");
		}
		
		Membership activeMembership = activeMemberships.get(0);
		Plan activePlan = activeMembership.getPlan();
		
		// Multi-Branch Access Check
		boolean isGlobalPlan = activePlan.getBranches().isEmpty();
		boolean hasBranchAccess = isGlobalPlan || activePlan.getBranches().stream().anyMatch(b -> b.getBranchId().equals(cls.getBranch().getBranchId()));
		if (!member.getHomeBranch().getBranchId().equals(cls.getBranch().getBranchId()) && !hasBranchAccess) {
			throw new BusinessRuleException("Your membership plan does not allow booking classes at this branch.");
		}

		// AC01: Plan eligibility check
		if (cls.getPlanEligibility() != null && !cls.getPlanEligibility().isBlank()) {
			String planElig = cls.getPlanEligibility().toUpperCase();
			String memberPlanType = activePlan.getEligibilityType().name();
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
						|| b.getBookingStatus() == ClassBooking.BookingStatus.WAITLISTED
						|| b.getBookingStatus() == ClassBooking.BookingStatus.PENDING_CONFIRMATION)
				.collect(Collectors.toList());

		for (ClassBooking existingBooking : memberActiveBookings) {
			Classes existingClass = existingBooking.getFitnessClass();
			if (existingClass.getClassId().equals(cls.getClassId())) {
				throw new BusinessRuleException("You already have a booking or waitlist spot for this class.");
			}
			// Check time overlap on same date range and weekdays
			if (datesOverlap(cls, existingClass) && weekdaysOverlap(cls, existingClass)) {
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
				cls.getClassId(), ClassBooking.BookingStatus.CONFIRMED) +
				bookingRepo.countByFitnessClassClassIdAndBookingStatus(
				cls.getClassId(), ClassBooking.BookingStatus.PENDING_CONFIRMATION);

		Optional<ClassBooking> existingOpt = bookingRepo.findByFitnessClassClassIdAndMemberMemberId(cls.getClassId(), member.getMemberId());
		ClassBooking booking;
		if (existingOpt.isPresent()) {
			booking = existingOpt.get();
			booking.setCancelledAt(null);
			booking.setWaitlistExpiration(null);
			booking.setOverrideBy(null);
			booking.setOverrideReason(null);
		} else {
			booking = ClassBooking.builder()
					.fitnessClass(cls)
					.member(member)
					.build();
		}

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

		// AC06: Send booking confirmation notification and email
		try {
			java.util.Map<String, Object> vars = new java.util.HashMap<>();
			vars.put("memberName", member.getMemName());
			vars.put("className", cls.getClassName());
			vars.put("classTime", cls.getClassTime().toString());
			vars.put("status", saved.getBookingStatus().name());

			String statusMsg = saved.getBookingStatus() == ClassBooking.BookingStatus.CONFIRMED
					? "Your booking is confirmed!"
					: "You've been added to the waitlist (position " + saved.getWaitlistPosition() + ")";

			NotificationEvent event = new NotificationEvent(
					this,
					member.getUser(),
					Notification.NotifType.BOOKING,
					"Booking Confirmation",
					vars,
					"/member/classes",
					"Booking: " + cls.getClassName(),
					statusMsg + " Class: " + cls.getClassName() + " at " + cls.getClassTime()
			);
			eventPublisher.publishEvent(event);
		} catch (Exception ignored) {
		}

		return mapper.map(saved, ClassBookingDTO.class);
	}

	public void cancelBooking(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		LocalDateTime classDateTime = getNextOccurrence(booking.getFitnessClass());
		
		if (LocalDateTime.now().isAfter(classDateTime)) {
			throw new BusinessRuleException("Cannot cancel a class that has already started.");
		}

		// Check if it is a late cancellation (less than cutoff hours before start)
		boolean isLateCancel = LocalDateTime.now().isAfter(classDateTime.minusHours(cancelCutoffHours));

		booking.setBookingStatus(ClassBooking.BookingStatus.CANCELLED);
		booking.setCancelledAt(LocalDateTime.now());
		bookingRepo.save(booking);

		// AC10: Audit log
		auditLogService.logForCurrentUser("ClassBooking", bookingId, AuditLog.Action.UPDATE,
				"status=CONFIRMED", "status=CANCELLED" + (isLateCancel ? " (LATE_CANCEL)" : ""));

		// Send cancellation notification and email
		try {
			java.util.Map<String, Object> vars = new java.util.HashMap<>();
			vars.put("memberName", booking.getMember().getMemName());
			vars.put("className", booking.getFitnessClass().getClassName());

			NotificationEvent event = new NotificationEvent(
					this,
					booking.getMember().getUser(),
					Notification.NotifType.CANCELLATION,
					"Class Cancellation",
					vars,
					"/member/classes",
					isLateCancel ? "Late Cancellation Infraction" : "Booking Cancelled",
					isLateCancel ? "You cancelled '" + booking.getFitnessClass().getClassName() + "' less than 2 hours before the start. This counts as an infraction."
								 : "Your booking for '" + booking.getFitnessClass().getClassName() + "' has been cancelled."
			);
			eventPublisher.publishEvent(event);
		} catch (Exception ignored) {
		}

		if (isLateCancel && isBookingBanned(booking.getMember().getMemberId())) {
			try {
				NotificationEvent event = new NotificationEvent(
						this,
						booking.getMember().getUser(),
						Notification.NotifType.GENERAL,
						"Booking Ban",
						null,
						"/member/dashboard",
						"Booking Ban Active",
						"You have been banned from booking classes for 7 days due to accumulating 3 infractions (late cancellations/no-shows) in 30 days."
				);
				eventPublisher.publishEvent(event);
			} catch (Exception ignored) {
			}
		}

		// Evict expired promotions first and promote next
		checkAndEvictExpiredWaitlistPromotions(booking.getFitnessClass().getClassId());
		promoteNextWaitlist(booking.getFitnessClass());
	}

	@org.springframework.transaction.annotation.Transactional
	public ClassBookingDTO acceptWaitlistPromotion(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		checkAndEvictExpiredWaitlistPromotions(booking.getFitnessClass().getClassId());

		// Re-fetch in case it got evicted
		booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		if (booking.getBookingStatus() != ClassBooking.BookingStatus.PENDING_CONFIRMATION) {
			throw new BusinessRuleException("This booking is not pending confirmation or the response window has expired.");
		}

		booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
		booking.setWaitlistPosition(null);
		booking.setWaitlistExpiration(null);
		ClassBooking saved = bookingRepo.save(booking);

		try {
			NotificationEvent event = new NotificationEvent(
					this,
					booking.getMember().getUser(),
					Notification.NotifType.BOOKING,
					"Booking Confirmed",
					null,
					"/member/classes",
					"Booking Confirmed!",
					"Your waitlist promotion for '" + booking.getFitnessClass().getClassName() + "' is confirmed."
			);
			eventPublisher.publishEvent(event);
		} catch (Exception ignored) {
		}

		return mapper.map(saved, ClassBookingDTO.class);
	}

	public ClassBookingDTO overrideBooking(ClassBookingDTO dto, Long overrideByUserId, String reason) {
		if (reason == null || reason.isBlank())
			throw new BusinessRuleException("Override reason is required.");

		SystemUser overrideUser = userRepo.findById(overrideByUserId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", overrideByUserId));

		Classes cls = classesRepo.findById(dto.getClassId())
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));

		Optional<ClassBooking> existingOpt = bookingRepo.findByFitnessClassClassIdAndMemberMemberId(cls.getClassId(), member.getMemberId());
		ClassBooking booking;
		if (existingOpt.isPresent()) {
			booking = existingOpt.get();
			booking.setCancelledAt(null);
			booking.setWaitlistExpiration(null);
			booking.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
			booking.setOverrideBy(overrideUser);
			booking.setOverrideReason(reason);
		} else {
			booking = ClassBooking.builder()
					.fitnessClass(cls)
					.member(member)
					.bookingStatus(ClassBooking.BookingStatus.CONFIRMED)
					.overrideBy(overrideUser)
					.overrideReason(reason)
					.build();
		}

		ClassBooking saved = bookingRepo.save(booking);

		auditLogService.logForCurrentUser("ClassBooking", saved.getBookingId(), AuditLog.Action.CREATE,
				null, "OVERRIDE booking by " + overrideUser.getUsername() + ": " + reason);

		return mapper.map(saved, ClassBookingDTO.class);
	}

	public ClassBookingDTO markNoShow(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		if (booking.getBookingStatus() != ClassBooking.BookingStatus.CONFIRMED)
			throw new BusinessRuleException("Only confirmed bookings can be marked as no-show.");

		booking.setBookingStatus(ClassBooking.BookingStatus.NO_SHOW);
		bookingRepo.save(booking);

		auditLogService.logForCurrentUser("ClassBooking", bookingId, AuditLog.Action.UPDATE,
				"status=CONFIRMED", "status=NO_SHOW");

		// Check active ban warning/triggers
		if (isBookingBanned(booking.getMember().getMemberId())) {
			try {
				NotificationEvent event = new NotificationEvent(
						this,
						booking.getMember().getUser(),
						Notification.NotifType.GENERAL,
						"Booking Ban",
						null,
						"/member/dashboard",
						"Booking Ban Active",
						"You have been banned from booking classes for 7 days due to accumulating 3 infractions (late cancellations/no-shows) in 30 days."
				);
				eventPublisher.publishEvent(event);
			} catch (Exception ignored) {
			}
		} else {
			long noShowCount = bookingRepo.findByMemberMemberId(booking.getMember().getMemberId())
					.stream()
					.filter(b -> b.getBookingStatus() == ClassBooking.BookingStatus.NO_SHOW)
					.count();
			if (noShowCount >= noShowPenaltyThreshold) {
				try {
					NotificationEvent event = new NotificationEvent(
							this,
							booking.getMember().getUser(),
							Notification.NotifType.GENERAL,
							"No Show Warning",
							null,
							"/member/dashboard",
							"No-Show Warning",
							"You have " + noShowCount + " no-shows. Repeated no-shows may result in booking restrictions."
					);
					eventPublisher.publishEvent(event);
				} catch (Exception ignored) {
				}
			}
		}

		return mapper.map(booking, ClassBookingDTO.class);
	}

	public List<ClassBookingDTO> getBookingsByMember(Long memberId) {
		checkAndEvictExpiredWaitlistPromotionsForMember(memberId);
		return bookingRepo.findByMemberMemberId(memberId).stream()
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}

	public List<ClassBookingDTO> getBookingsByClass(Long classId) {
		checkAndEvictExpiredWaitlistPromotions(classId);
		return bookingRepo.findByFitnessClassClassId(classId).stream()
				.filter(b -> b.getMember().getHomeBranch().getBranchId().equals(b.getFitnessClass().getBranch().getBranchId()))
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}

	private void checkAndEvictExpiredWaitlistPromotions(Long classId) {
		LocalDateTime now = LocalDateTime.now();
		List<ClassBooking> expiredList = bookingRepo.findByFitnessClassClassId(classId).stream()
				.filter(b -> b.getBookingStatus() == ClassBooking.BookingStatus.PENDING_CONFIRMATION)
				.filter(b -> b.getWaitlistExpiration() != null && now.isAfter(b.getWaitlistExpiration()))
				.collect(Collectors.toList());

		for (ClassBooking expired : expiredList) {
			expired.setBookingStatus(ClassBooking.BookingStatus.CANCELLED);
			expired.setWaitlistPosition(null);
			expired.setWaitlistExpiration(null);
			bookingRepo.save(expired);

			try {
				NotificationEvent event = new NotificationEvent(
						this,
						expired.getMember().getUser(),
						Notification.NotifType.BOOKING,
						"Waitlist Expired",
						null,
						"/member/classes",
						"Waitlist Promotion Expired",
						"You failed to accept the promotion for '" + expired.getFitnessClass().getClassName() + "' within 15 minutes."
				);
				eventPublisher.publishEvent(event);
			} catch (Exception ignored) {
			}

			promoteNextWaitlist(expired.getFitnessClass());
		}
	}

	private void checkAndEvictExpiredWaitlistPromotionsForMember(Long memberId) {
		LocalDateTime now = LocalDateTime.now();
		List<ClassBooking> expiredList = bookingRepo.findByMemberMemberId(memberId).stream()
				.filter(b -> b.getBookingStatus() == ClassBooking.BookingStatus.PENDING_CONFIRMATION)
				.filter(b -> b.getWaitlistExpiration() != null && now.isAfter(b.getWaitlistExpiration()))
				.collect(Collectors.toList());

		for (ClassBooking expired : expiredList) {
			expired.setBookingStatus(ClassBooking.BookingStatus.CANCELLED);
			expired.setWaitlistPosition(null);
			expired.setWaitlistExpiration(null);
			bookingRepo.save(expired);

			try {
				NotificationEvent event = new NotificationEvent(
						this,
						expired.getMember().getUser(),
						Notification.NotifType.BOOKING,
						"Waitlist Expired",
						null,
						"/member/classes",
						"Waitlist Promotion Expired",
						"You failed to accept the promotion for '" + expired.getFitnessClass().getClassName() + "' within 15 minutes."
				);
				eventPublisher.publishEvent(event);
			} catch (Exception ignored) {
			}

			promoteNextWaitlist(expired.getFitnessClass());
		}
	}

	private void promoteNextWaitlist(Classes fitnessClass) {
		LocalDateTime classDateTime = getNextOccurrence(fitnessClass);
		if (LocalDateTime.now().isAfter(classDateTime.minusMinutes(15))) {
			return; // Locked 15 mins before start
		}

		bookingRepo.findFirstByFitnessClassClassIdAndBookingStatusOrderByWaitlistPositionAsc(
				fitnessClass.getClassId(), ClassBooking.BookingStatus.WAITLISTED)
				.ifPresent(next -> {
					if (LocalDateTime.now().isBefore(classDateTime.minusHours(2))) {
						next.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
						next.setWaitlistPosition(null);
						next.setWaitlistExpiration(null);
						bookingRepo.save(next);

						try {
							NotificationEvent event = new NotificationEvent(
									this,
									next.getMember().getUser(),
									Notification.NotifType.BOOKING,
									"Waitlist Promotion",
									null,
									"/member/classes",
									"Waitlist Promotion!",
									"You've been promoted from the waitlist for '"
											+ next.getFitnessClass().getClassName() + "'. Your booking is now confirmed!"
							);
							eventPublisher.publishEvent(event);
						} catch (Exception ignored) {
						}
					} else {
						next.setBookingStatus(ClassBooking.BookingStatus.PENDING_CONFIRMATION);
						next.setWaitlistExpiration(LocalDateTime.now().plusMinutes(15));
						bookingRepo.save(next);

						try {
							NotificationEvent event = new NotificationEvent(
									this,
									next.getMember().getUser(),
									Notification.NotifType.BOOKING,
									"Waitlist Spot Open",
									null,
									"/member/classes",
									"Waitlist Spot Open - Action Required!",
									"A spot opened up for '" + next.getFitnessClass().getClassName() + "'. You have 15 minutes to accept the booking!"
							);
							eventPublisher.publishEvent(event);
						} catch (Exception ignored) {
						}
					}
				});
	}

	private boolean isBookingBanned(Long memberId) {
		LocalDateTime now = LocalDateTime.now();
		List<ClassBooking> infractions = bookingRepo.findByMemberMemberId(memberId).stream()
				.filter(b -> {
					if (b.getBookingStatus() == ClassBooking.BookingStatus.NO_SHOW) {
						LocalDateTime classTime = b.getFitnessClass().getStartDate().atTime(b.getFitnessClass().getClassTime());
						return classTime.isAfter(now.minusDays(30));
					}
					if (b.getBookingStatus() == ClassBooking.BookingStatus.CANCELLED && b.getCancelledAt() != null) {
						LocalDateTime classTime = b.getFitnessClass().getStartDate().atTime(b.getFitnessClass().getClassTime());
						if (classTime.isAfter(now.minusDays(30))) {
							return b.getCancelledAt().isAfter(classTime.minusHours(cancelCutoffHours));
						}
					}
					return false;
				})
				.sorted((b1, b2) -> getInfractionTime(b2).compareTo(getInfractionTime(b1))) // descending (most recent first)
				.collect(Collectors.toList());

		if (infractions.size() >= 3) {
			LocalDateTime mostRecentInfractionTime = getInfractionTime(infractions.get(0));
			return now.isBefore(mostRecentInfractionTime.plusDays(7));
		}
		return false;
	}

	private LocalDateTime getInfractionTime(ClassBooking b) {
		if (b.getBookingStatus() == ClassBooking.BookingStatus.CANCELLED && b.getCancelledAt() != null) {
			return b.getCancelledAt();
		}
		return getNextOccurrence(b.getFitnessClass());
	}

	private boolean weekdaysOverlap(Classes a, Classes b) {
		if (a.getWeekdays() == null || b.getWeekdays() == null) return true;
		String[] aDays = a.getWeekdays().toUpperCase().split(",");
		String[] bDays = b.getWeekdays().toUpperCase().split(",");
		for (String ad : aDays) {
			for (String bd : bDays) {
				if (ad.trim().equals(bd.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean datesOverlap(Classes a, Classes b) {
		return !a.getStartDate().isAfter(b.getEndDate()) && !a.getEndDate().isBefore(b.getStartDate());
	}

	private LocalDateTime getNextOccurrence(Classes cls) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime time = cls.getClassTime();
		if (cls.getWeekdays() == null || cls.getWeekdays().isBlank()) {
			return cls.getStartDate().atTime(time);
		}
		
		String[] days = cls.getWeekdays().toUpperCase().split(",");
		LocalDateTime bestDate = null;
		for (String dayStr : days) {
			java.time.DayOfWeek targetDay = getDayOfWeek(dayStr.trim());
			if (targetDay == null) continue;
			
			LocalDateTime candidate = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(targetDay)).with(time);
			if (candidate.isBefore(now.plusMinutes(15)) && candidate.getDayOfWeek() == now.getDayOfWeek()) {
				candidate = candidate.plusWeeks(1);
			} else if (candidate.isBefore(now)) {
				candidate = candidate.plusWeeks(1);
			}
			
			if (bestDate == null || candidate.isBefore(bestDate)) {
				bestDate = candidate;
			}
		}
		
		return bestDate != null ? bestDate : cls.getStartDate().atTime(time);
	}

	private java.time.DayOfWeek getDayOfWeek(String dayStr) {
		if (dayStr.length() < 3) return null;
		switch (dayStr.substring(0, 3)) {
			case "MON": return java.time.DayOfWeek.MONDAY;
			case "TUE": return java.time.DayOfWeek.TUESDAY;
			case "WED": return java.time.DayOfWeek.WEDNESDAY;
			case "THU": return java.time.DayOfWeek.THURSDAY;
			case "FRI": return java.time.DayOfWeek.FRIDAY;
			case "SAT": return java.time.DayOfWeek.SATURDAY;
			case "SUN": return java.time.DayOfWeek.SUNDAY;
		}
		return null;
	}
}