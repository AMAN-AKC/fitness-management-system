package com.fitness.repository;

import com.fitness.entity.HealthConsent;
import com.fitness.entity.HealthConsent.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthConsentRepository extends JpaRepository<HealthConsent, Long> {
	List<HealthConsent> findByMemberMemberId(Long memberId);

	Optional<HealthConsent> findByMemberMemberIdAndStatus(Long memberId, Status status);

	boolean existsByMemberMemberIdAndStatus(Long memberId, Status status);

	List<HealthConsent> findByMemberMemberIdOrderByAcknowledgedAtDesc(Long memberId);

	Optional<HealthConsent> findTopByMemberMemberIdOrderByAcknowledgedAtDesc(Long memberId);

	List<HealthConsent> findByAcknowledgedAtBefore(LocalDateTime acknowledgedAt);

	@Query("select c.formVersion, c.status, count(c), sum(case when c.parqResponses like '%true%' then 1 else 0 end) from HealthConsent c group by c.formVersion, c.status")
	List<Object[]> summarizeAnonymizedStats();
}
