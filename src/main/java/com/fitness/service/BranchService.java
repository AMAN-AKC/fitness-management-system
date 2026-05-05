package com.fitness.service;

import com.fitness.dto.BranchDTO;
import com.fitness.entity.Branch;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public BranchDTO createBranch(BranchDTO dto) {
		if (branchRepo.existsByBranchName(dto.getBranchName()))
			throw new DuplicateResourceException("Branch", "name", dto.getBranchName());
		Branch branch = mapper.map(dto, Branch.class);
		branch.setIsActive(true);
		return mapper.map(branchRepo.save(branch), BranchDTO.class);
	}

	public List<BranchDTO> getAllBranches() {
		return branchRepo.findAll().stream()
				.map(b -> mapper.map(b, BranchDTO.class))
				.collect(Collectors.toList());
	}

	public List<BranchDTO> getActiveBranches() {
		return branchRepo.findByIsActiveTrue().stream()
				.map(b -> mapper.map(b, BranchDTO.class))
				.collect(Collectors.toList());
	}

	public BranchDTO getBranchById(Long id) {
		return mapper.map(findById(id), BranchDTO.class);
	}

	public BranchDTO updateBranch(Long id, BranchDTO dto) {
		Branch branch = findById(id);
		mapper.map(dto, branch);
		return mapper.map(branchRepo.save(branch), BranchDTO.class);
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
