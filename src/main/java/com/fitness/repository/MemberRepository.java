package com.fitness.repository;

import com.fitness.entity.Member;
import com.fitness.entity.Member.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);

	Optional<Member> findByPhone(String phone);

	Optional<Member> findByUserUserId(Long userId);

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

	List<Member> findByStatus(Status status);

	List<Member> findByHomeBranchBranchId(Long branchId);
}
