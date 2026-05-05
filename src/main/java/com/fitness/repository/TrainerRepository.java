package com.fitness.repository;

import com.fitness.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
	Optional<Trainer> findByUserUserId(Long userId);

	List<Trainer> findByBranchBranchId(Long branchId);

	List<Trainer> findByIsActiveTrue();
}