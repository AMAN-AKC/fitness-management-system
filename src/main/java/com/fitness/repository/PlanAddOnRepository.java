package com.fitness.repository;

import com.fitness.entity.PlanAddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanAddOnRepository extends JpaRepository<PlanAddOn, Long> {
	List<PlanAddOn> findByPlanPlanId(Long planId);

	List<PlanAddOn> findByAddOnAddonId(Long addonId);

	boolean existsByPlanPlanIdAndAddOnAddonId(Long planId, Long addonId);
}
