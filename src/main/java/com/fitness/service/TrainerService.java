package com.fitness.service;

import com.fitness.dto.TrainerDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerService {

	private final TrainerRepository trainerRepo;
	private final SystemUserRepository userRepo;
	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public TrainerDTO createTrainer(TrainerDTO dto) {
		SystemUser user = userRepo.findById(dto.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", dto.getUserId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
		Trainer trainer = mapper.map(dto, Trainer.class);
		trainer.setUser(user);
		trainer.setBranch(branch);
		trainer.setIsActive(true);
		return mapper.map(trainerRepo.save(trainer), TrainerDTO.class);
	}

	public List<TrainerDTO> getAllTrainers() {
		return trainerRepo.findAll().stream().map(t -> mapper.map(t, TrainerDTO.class)).collect(Collectors.toList());
	}

	public List<TrainerDTO> getTrainersByBranch(Long branchId) {
		return trainerRepo.findByBranchBranchId(branchId).stream().map(t -> mapper.map(t, TrainerDTO.class))
				.collect(Collectors.toList());
	}

	public TrainerDTO getTrainerById(Long id) {
		return mapper.map(findById(id), TrainerDTO.class);
	}

	public TrainerDTO updateTrainer(Long id, TrainerDTO dto) {
		Trainer trainer = findById(id);
		mapper.map(dto, trainer);
		return mapper.map(trainerRepo.save(trainer), TrainerDTO.class);
	}

	public void deactivateTrainer(Long id) {
		Trainer t = findById(id);
		t.setIsActive(false);
		trainerRepo.save(t);
	}

	private Trainer findById(Long id) {
		return trainerRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", id));
	}
}
