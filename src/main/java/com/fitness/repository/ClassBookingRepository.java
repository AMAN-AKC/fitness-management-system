package com.fitness.repository;

import com.fitness.entity.ClassBooking;
import com.fitness.entity.ClassBooking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassBookingRepository extends JpaRepository<ClassBooking, Long> {
	List<ClassBooking> findByMemberMemberId(Long memberId);

	List<ClassBooking> findByFitnessClassClassId(Long classId);

	List<ClassBooking> findByFitnessClassClassIdAndBookingStatus(Long classId, BookingStatus status);

	Optional<ClassBooking> findByFitnessClassClassIdAndMemberMemberIdAndBookingStatusNot(
			Long classId, Long memberId, BookingStatus status);

	long countByFitnessClassClassIdAndBookingStatus(Long classId, BookingStatus status);

	Optional<ClassBooking> findFirstByFitnessClassClassIdAndBookingStatusOrderByWaitlistPositionAsc(
			Long classId, BookingStatus status);
}
