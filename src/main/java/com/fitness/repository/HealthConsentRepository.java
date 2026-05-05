package com.fitness.repository;

import com.fitness.entity.HealthConsent;
import com.fitness.entity.HealthConsent.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthConsentRepository extends JpaRepository<HealthConsent, Long> {
	List<HealthConsent> findByMemberMemberId(Long memberId);

	Optional<HealthConsent> findByMemberMemberIdAndStatus(Long memberId, Status status);

	boolean existsByMemberMemberIdAndStatus(Long memberId, Status status);
}