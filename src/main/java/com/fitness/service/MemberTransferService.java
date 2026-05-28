package com.fitness.service;

import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.entity.MemberTransferLog;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.MemberTransferLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberTransferService {

	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final MemberTransferLogRepository transferRepo;

	private String getCurrentUsername() {
		try {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		} catch (Exception e) {
			return "SYSTEM";
		}
	}

	public void transferMember(Long memberId, Long targetBranchId, String reason) {
		Member member = memberRepo.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("Member not found"));

		Branch targetBranch = branchRepo.findById(targetBranchId)
				.orElseThrow(() -> new IllegalArgumentException("Target branch not found"));

		Branch fromBranch = member.getHomeBranch();

		if (fromBranch != null && fromBranch.getBranchId().equals(targetBranchId)) {
			throw new IllegalStateException("Member is already assigned to this branch.");
		}

		member.setHomeBranch(targetBranch);
		memberRepo.save(member);

		MemberTransferLog logEntry = MemberTransferLog.builder()
				.member(member)
				.fromBranch(fromBranch != null ? fromBranch : targetBranch) // Fallback if no home branch
				.toBranch(targetBranch)
				.reason(reason)
				.transferredBy(getCurrentUsername())
				.transferDate(LocalDateTime.now())
				.build();

		transferRepo.save(logEntry);
		log.info("Member {} transferred to branch {}.", memberId, targetBranchId);
	}
}
