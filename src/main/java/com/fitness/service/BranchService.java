package com.fitness.service;

import com.fitness.dto.BranchDTO;
import com.fitness.entity.Branch;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.entity.Member;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

	private final BranchRepository branchRepo;
	private final MemberRepository memberRepo;
	private final ModelMapper mapper;

	private BranchDTO mapToDTO(Branch branch) {
		BranchDTO dto = mapper.map(branch, BranchDTO.class);
		if (branch.getBranchId() != null) {
			long activeCount = memberRepo.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, branch.getBranchId());
			dto.setActiveMembersCount((int) activeCount);
		} else {
			dto.setActiveMembersCount(0);
		}
		return dto;
	}

	public BranchDTO createBranch(BranchDTO dto) {
		if (branchRepo.existsByBranchName(dto.getBranchName()))
			throw new DuplicateResourceException("Branch", "name", dto.getBranchName());
		Branch branch = mapper.map(dto, Branch.class);
		branch.setIsActive(true);
		return mapToDTO(branchRepo.save(branch));
	}

	public List<BranchDTO> getAllBranches() {
		return branchRepo.findAll().stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	public List<BranchDTO> getActiveBranches() {
		return branchRepo.findByIsActiveTrue().stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	public BranchDTO getBranchById(Long id) {
		return mapToDTO(findById(id));
	}

	public BranchDTO updateBranch(Long id, BranchDTO dto) {
		Branch branch = findById(id);
		mapper.map(dto, branch);
		return mapToDTO(branchRepo.save(branch));
	}

	public void deactivateBranch(Long id) {
		Branch branch = findById(id);
		branch.setIsActive(false);
		branchRepo.save(branch);
	}

	private Branch findById(Long id) {
		return branchRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
	}
}
