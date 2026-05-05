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

	List<ClassBooking> findByClassesClassId(Long classId);

	List<ClassBooking> findByClassesClassIdAndBookingStatus(Long classId, BookingStatus status);

	Optional<ClassBooking> findByClassesClassIdAndMemberMemberIdAndBookingStatusNot(
			Long classId, Long memberId, BookingStatus status);

	long countByClassesClassIdAndBookingStatus(Long classId, BookingStatus status);

	Optional<ClassBooking> findFirstByClassesClassIdAndBookingStatusOrderByWaitlistPositionAsc(
			Long classId, BookingStatus status);
}
