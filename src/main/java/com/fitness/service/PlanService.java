package com.fitness.service;

import com.fitness.dto.PlanDTO;
import com.fitness.entity.Plan;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepo;
	private final MembershipRepository membershipRepo;
	private final ModelMapper mapper;

	public PlanDTO createPlan(PlanDTO dto) {
		if (planRepo.existsByPlanName(dto.getPlanName()))
			throw new DuplicateResourceException("Plan", "name", dto.getPlanName());
		Plan plan = mapper.map(dto, Plan.class);
		plan.setIsActive(true);
		plan.setVersion(1);
		return mapper.map(planRepo.save(plan), PlanDTO.class);
	}

	public List<PlanDTO> getAllPlans() {
		return planRepo.findAll().stream().map(p -> mapper.map(p, PlanDTO.class)).collect(Collectors.toList());
	}

	public List<PlanDTO> getActivePlans() {
		return planRepo.findByIsActiveTrue().stream().map(p -> mapper.map(p, PlanDTO.class))
				.collect(Collectors.toList());
	}

	public PlanDTO getPlanById(Long id) {
		return mapper.map(findById(id), PlanDTO.class);
	}

	public PlanDTO updatePlan(Long id, PlanDTO dto) {
		Plan plan = findById(id);
		mapper.map(dto, plan);
		plan.setVersion(plan.getVersion() + 1);
		return mapper.map(planRepo.save(plan), PlanDTO.class);
	}

	public void deactivatePlan(Long id) {
		Plan plan = findById(id);
		boolean inUse = !membershipRepo.findByStatus(com.fitness.entity.Membership.Status.ACTIVE).stream()
				.filter(m -> m.getPlan().getPlanId().equals(id))
				.toList().isEmpty();
		if (inUse)
			throw new BusinessRuleException(
					"Plan is currently in use by active memberships. Cannot delete — deactivate instead.");
		plan.setIsActive(false);
		planRepo.save(plan);
	}

	private Plan findById(Long id) {
		return planRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));
	}
}
