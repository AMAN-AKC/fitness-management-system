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
		Trainer trainer = mapper.map(dto, Trainer.class);
		trainer.setUser(user);
		
		if (dto.getBranchId() != null) {
			Branch branch = branchRepo.findById(dto.getBranchId())
					.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
			trainer.setBranch(branch);
		}
		
		trainer.setIsActive(true);
		return convertToDto(trainerRepo.save(trainer));
	}

	public List<TrainerDTO> getAllTrainers() {
		return trainerRepo.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<TrainerDTO> getTrainersByBranch(Long branchId) {
		return trainerRepo.findByBranchBranchId(branchId).stream().map(this::convertToDto)
				.collect(Collectors.toList());
	}

	public TrainerDTO getTrainerById(Long id) {
		return convertToDto(findById(id));
	}

	public TrainerDTO updateTrainer(Long id, TrainerDTO dto) {
		Trainer trainer = findById(id);
		if (dto.getBio() != null) trainer.setBio(dto.getBio());
		if (dto.getCertifications() != null) trainer.setCertifications(dto.getCertifications());
		if (dto.getSpecialties() != null) trainer.setSpecialties(dto.getSpecialties());
		if (dto.getAcceptingPtClients() != null) trainer.setAcceptingPtClients(dto.getAcceptingPtClients());
		if (dto.getAvailability() != null) trainer.setAvailability(dto.getAvailability());
		if (dto.getIsActive() != null) trainer.setIsActive(dto.getIsActive());
		return convertToDto(trainerRepo.save(trainer));
	}

	public void deactivateTrainer(Long id) {
		Trainer t = findById(id);
		t.setIsActive(false);
		trainerRepo.save(t);
	}

	public TrainerDTO getTrainerByUserId(Long userId) {
		Trainer trainer = trainerRepo.findByUserUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "userId", userId));
		return convertToDto(trainer);
	}

	private TrainerDTO convertToDto(Trainer trainer) {
		TrainerDTO dto = mapper.map(trainer, TrainerDTO.class);
		if (trainer.getUser() != null) {
			dto.setUserId(trainer.getUser().getUserId());
			dto.setTrainerName(trainer.getUser().getFullName() != null && !trainer.getUser().getFullName().trim().isEmpty()
					? trainer.getUser().getFullName()
					: trainer.getUser().getUsername());
		}
		if (trainer.getBranch() != null) {
			dto.setBranchId(trainer.getBranch().getBranchId());
		}
		return dto;
	}

	private Trainer findById(Long id) {
		return trainerRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", id));
	}
}

