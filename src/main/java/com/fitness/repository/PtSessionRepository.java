package com.fitness.repository;

import com.fitness.entity.PtSession;
import com.fitness.entity.PtSession.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PtSessionRepository extends JpaRepository<PtSession, Long> {
	List<PtSession> findByMemberMemberId(Long memberId);

	List<PtSession> findByTrainerTrainerId(Long trainerId);

	List<PtSession> findByStatus(Status status);

	@Query("SELECT s FROM PtSession s WHERE s.trainer.trainerId = :trainerId " +
			"AND s.status NOT IN ('CANCELLED', 'DECLINED') " +
			"AND s.scheduledAt < :endTime " +
			"AND (s.scheduledAt + s.durationMins MINUTE) > :startTime")
	List<PtSession> findOverlappingForTrainer(@Param("trainerId") Long trainerId,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime);
}
