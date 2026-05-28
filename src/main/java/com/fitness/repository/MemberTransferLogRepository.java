package com.fitness.repository;

import com.fitness.entity.MemberTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberTransferLogRepository extends JpaRepository<MemberTransferLog, Long> {
	List<MemberTransferLog> findByMember_MemberIdOrderByTransferDateDesc(Long memberId);
}
