package com.fitness.repository;

import com.fitness.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    FeatureFlag findByFlagName(String flagName);
}
