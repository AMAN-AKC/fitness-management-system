package com.fitness.service;

import com.fitness.dto.PlanDTO;
import com.fitness.entity.Plan;
import com.fitness.entity.AuditLog.Action;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepo;
	private final MembershipRepository membershipRepo;
	private final AuditLogService auditLogService;
	private final ModelMapper mapper;

	public PlanDTO createPlan(PlanDTO dto) {
		if (planRepo.existsByPlanName(dto.getPlanName()))
			throw new DuplicateResourceException("Plan", "name", dto.getPlanName());
		Plan plan = mapper.map(dto, Plan.class);
		plan.setIsActive(true);
		plan.setVersion(1);
		Plan saved = planRepo.save(plan);
		auditLogService.logForCurrentUser("Plan", saved.getPlanId(), Action.CREATE, 
			"Plan created: " + saved.getPlanName(), null);
		return mapper.map(saved, PlanDTO.class);
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
		String oldValues = String.format("price=%s, taxPercent=%s, version=%d", 
			plan.getPrice(), plan.getTaxPercent(), plan.getVersion());
		mapper.map(dto, plan);
		plan.setVersion(plan.getVersion() + 1);
		Plan updated = planRepo.save(plan);
		String newValues = String.format("price=%s, taxPercent=%s, version=%d", 
			updated.getPrice(), updated.getTaxPercent(), updated.getVersion());
		auditLogService.logForCurrentUser("Plan", id, Action.UPDATE, oldValues, newValues);
		return mapper.map(updated, PlanDTO.class);
	}

	public void deactivatePlan(Long id) {
		Plan plan = findById(id);
		boolean inUse = !membershipRepo.findByStatus(com.fitness.entity.Membership.Status.ACTIVE).stream()
				.filter(m -> m.getPlan().getPlanId().equals(id))
				.toList().isEmpty();
		// AC09: allow deactivation even if in use, but prevent physical deletion (not implemented here)
		// We just log a warning if in use
		if (inUse) {
			log.info("Deactivating plan {} which has active memberships", id);
		}
		plan.setIsActive(false);
		planRepo.save(plan);
		auditLogService.logForCurrentUser("Plan", id, Action.UPDATE, "isActive=true", "isActive=false");
	}

	private Plan findById(Long id) {
		return planRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));
	}
}
