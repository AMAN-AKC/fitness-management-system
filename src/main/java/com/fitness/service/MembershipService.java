package com.fitness.service;

import com.fitness.dto.MembershipDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

	private final MembershipRepository membershipRepo;
	private final MemberRepository memberRepo;
	private final PlanRepository planRepo;
	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public MembershipDTO createMembership(MembershipDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Plan plan = planRepo.findById(dto.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", dto.getPlanId()));
		if (!plan.getIsActive())
			throw new BusinessRuleException("Selected plan is not active.");
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		Membership membership = mapper.map(dto, Membership.class);
		membership.setMember(member);
		membership.setPlan(plan);
		membership.setBranch(branch);
		membership.setStatus(Membership.Status.ACTIVE);
		membership.setStartDate(LocalDate.now());
		membership.setEndDate(LocalDate.now().plusDays(plan.getDurationDays()));
		membership.setDuration(plan.getDurationDays());
		membership.setPrice(plan.getPrice());

		member.setStatus(Member.Status.ACTIVE);
		memberRepo.save(member);

		return mapper.map(membershipRepo.save(membership), MembershipDTO.class);
	}

	public List<MembershipDTO> getMembershipsByMember(Long memberId) {
		return membershipRepo.findByMemberMemberId(memberId).stream()
				.map(m -> mapper.map(m, MembershipDTO.class)).collect(Collectors.toList());
	}

	public MembershipDTO getMembershipById(Long id) {
		return mapper.map(findById(id), MembershipDTO.class);
	}

	public List<MembershipDTO> getExpiringMemberships(int daysAhead) {
		LocalDate threshold = LocalDate.now().plusDays(daysAhead);
		return membershipRepo.findByEndDateBeforeAndStatus(threshold, Membership.Status.ACTIVE)
				.stream().map(m -> mapper.map(m, MembershipDTO.class)).collect(Collectors.toList());
	}

	private Membership findById(Long id) {
		return membershipRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));
	}
}
