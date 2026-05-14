package com.fitness.repository;

import com.fitness.entity.Membership;
import com.fitness.entity.Membership.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
	List<Membership> findByMemberMemberId(Long memberId);

	List<Membership> findByMemberMemberIdAndStatus(Long memberId, Status status);

	List<Membership> findByStatus(Status status);

	List<Membership> findByEndDateBeforeAndStatus(LocalDate date, Status status);
}
