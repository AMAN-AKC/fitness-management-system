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
import java.math.BigDecimal;
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
	private final ProratedPriceService proratedPriceService;
	private final AuditLogService auditLogService;

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

		Membership saved = membershipRepo.save(membership);

		auditLogService.logForCurrentUser("Membership", saved.getMemId(), AuditLog.Action.CREATE,
				null,
				"Plan purchased: " + plan.getPlanName() + " for member " + member.getMemName());

		return mapper.map(saved, MembershipDTO.class);
	}

	/**
	 * Upgrade existing membership to a new plan
	 */
	public MembershipDTO upgradeMembership(Long memberId, Long newPlanId, BigDecimal discountAmount) {
		if (!memberRepo.existsById(memberId)) {
			throw new ResourceNotFoundException("Member", "id", memberId);
		}
		List<Membership> activeList = membershipRepo.findByMemberMemberIdAndStatus(memberId, Membership.Status.ACTIVE);
		if (activeList.isEmpty()) {
			throw new ResourceNotFoundException("Active membership", "memberId", memberId);
		}
		Membership currentMembership = activeList.get(0);
		Plan newPlan = planRepo.findById(newPlanId)
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", newPlanId));

		// Calculate remaining value and upgrade cost
		BigDecimal remainingValue = proratedPriceService.calculateRemainingValue(currentMembership);
		BigDecimal upgradeCost = newPlan.getPrice().subtract(remainingValue).subtract(
				discountAmount != null ? discountAmount : java.math.BigDecimal.ZERO);
		upgradeCost = upgradeCost.max(java.math.BigDecimal.ZERO);

		// Update membership
		currentMembership.setEndDate(java.time.LocalDate.now().plusDays(newPlan.getDurationDays()));
		currentMembership.setDuration(newPlan.getDurationDays());
		currentMembership.setPlan(newPlan);
		currentMembership.setPrice(newPlan.getPrice());
		currentMembership.setDiscountAmount(discountAmount != null ? discountAmount : java.math.BigDecimal.ZERO);

		membershipRepo.save(currentMembership);

		auditLogService.logForCurrentUser("Membership", currentMembership.getMemId(), AuditLog.Action.UPDATE,
				"Plan: " + currentMembership.getPlan().getPlanName(),
				"Plan upgraded to " + newPlan.getPlanName() + " with upgrade cost ₹" + upgradeCost);

		return mapper.map(currentMembership, MembershipDTO.class);
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
