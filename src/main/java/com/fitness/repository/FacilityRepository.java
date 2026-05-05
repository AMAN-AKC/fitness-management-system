package com.fitness.repository;

import com.fitness.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
	List<Facility> findByBranchBranchId(Long branchId);

	List<Facility> findByIsActiveTrue();

	List<Facility> findByBranchBranchIdAndIsActiveTrue(Long branchId);
}
