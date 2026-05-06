package com.fitness.service;

import com.fitness.dto.ClassBookingDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassBookingService {

	private final ClassBookingRepository bookingRepo;
	private final ClassesRepository classesRepo;
	private final MemberRepository memberRepo;
	private final ModelMapper mapper;
	private static final int CANCEL_CUTOFF_HOURS = 2;

	public ClassBookingDTO bookClass(ClassBookingDTO dto) {
		Classes cls = classesRepo.findById(dto.getClassId())
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", dto.getClassId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));

		if (member.getStatus() != Member.Status.ACTIVE)
			throw new BusinessRuleException("Only active members can book classes.");
		if (cls.getStatus() != Classes.Status.ACTIVE)
			throw new BusinessRuleException("This class is not available for booking.");

		// Prevent double-booking
		Optional<ClassBooking> existing = bookingRepo
				.findByFitnessClassClassIdAndMemberMemberIdAndBookingStatusNot(
						cls.getClassId(), member.getMemberId(), ClassBooking.BookingStatus.CANCELLED);
		if (existing.isPresent())
			throw new BusinessRuleException("You already have a booking or waitlist spot for this class.");

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
		return mapper.map(bookingRepo.save(booking), ClassBookingDTO.class);
	}

	public void cancelBooking(Long bookingId) {
		ClassBooking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

		LocalDateTime classDateTime = booking.getFitnessClass().getStartDate()
				.atTime(booking.getFitnessClass().getClassTime());
		if (LocalDateTime.now().isAfter(classDateTime.minusHours(CANCEL_CUTOFF_HOURS)))
			throw new BusinessRuleException("Cannot cancel within " + CANCEL_CUTOFF_HOURS + " hours of the class.");

		booking.setBookingStatus(ClassBooking.BookingStatus.CANCELLED);
		booking.setCancelledAt(LocalDateTime.now());
		bookingRepo.save(booking);

		// Promote next on waitlist (FIFO)
		bookingRepo.findFirstByFitnessClassClassIdAndBookingStatusOrderByWaitlistPositionAsc(
				booking.getFitnessClass().getClassId(), ClassBooking.BookingStatus.WAITLISTED)
				.ifPresent(next -> {
					next.setBookingStatus(ClassBooking.BookingStatus.CONFIRMED);
					next.setWaitlistPosition(null);
					bookingRepo.save(next);
				});
	}

	public List<ClassBookingDTO> getBookingsByMember(Long memberId) {
		return bookingRepo.findByMemberMemberId(memberId).stream()
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}

	public List<ClassBookingDTO> getBookingsByClass(Long classId) {
		return bookingRepo.findByFitnessClassClassId(classId).stream()
				.map(b -> mapper.map(b, ClassBookingDTO.class)).collect(Collectors.toList());
	}
}