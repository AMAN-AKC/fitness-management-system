package com.fitness.repository;

import com.fitness.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	List<Attendance> findByMemberMemberId(Long memberId);

	List<Attendance> findByBranchBranchId(Long branchId);

	List<Attendance> findByCheckInTimeBetween(LocalDateTime from, LocalDateTime to);

	boolean existsByMemberMemberIdAndCheckInTimeBetween(
			Long memberId, LocalDateTime from, LocalDateTime to);
}
