package com.fitness.repository;

import com.fitness.entity.Classes;
import com.fitness.entity.Classes.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Long> {
	List<Classes> findByBranchBranchId(Long branchId);

	List<Classes> findByTrainerTrainerId(Long trainerId);

	List<Classes> findByStatus(Status status);

	@Query(value = "SELECT c.* FROM classes c WHERE c.facility_id = :roomId " +
			"AND c.status = 'ACTIVE' " +
			"AND c.class_time < :endTime " +
			"AND ADDTIME(c.class_time, SEC_TO_TIME(c.duration_mins * 60)) > :startTime", nativeQuery = true)
	List<Classes> findConflictingByRoom(@Param("roomId") Long roomId,
			@Param("startTime") LocalTime startTime,
			@Param("endTime") LocalTime endTime);

	@Query(value = "SELECT c.* FROM classes c WHERE c.trainer_id = :trainerId " +
			"AND c.status = 'ACTIVE' " +
			"AND c.class_time < :endTime " +
			"AND ADDTIME(c.class_time, SEC_TO_TIME(c.duration_mins * 60)) > :startTime", nativeQuery = true)
	List<Classes> findConflictingByTrainer(@Param("trainerId") Long trainerId,
			@Param("startTime") LocalTime startTime,
			@Param("endTime") LocalTime endTime);
}
