package com.fitness.repository;

import com.fitness.entity.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
    // Single row always has policyId = 1; no custom queries needed
}
