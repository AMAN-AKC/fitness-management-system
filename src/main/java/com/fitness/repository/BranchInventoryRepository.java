package com.fitness.repository;

import com.fitness.entity.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
	List<BranchInventory> findByBranch_BranchId(Long branchId);
}
