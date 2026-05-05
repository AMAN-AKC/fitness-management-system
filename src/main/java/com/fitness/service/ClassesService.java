package com.fitness.service;

import com.fitness.dto.ClassesDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassesService {

	private final ClassesRepository classesRepo;
	private final TrainerRepository trainerRepo;
	private final FacilityRepository facilityRepo;
	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public ClassesDTO createClass(ClassesDTO dto) {
		Trainer trainer = trainerRepo.findById(dto.getTrainerId())
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", dto.getTrainerId()));
		Facility room = facilityRepo.findById(dto.getRoomId())
				.orElseThrow(() -> new ResourceNotFoundException("Facility", "id", dto.getRoomId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		LocalTime start = LocalTime.parse(dto.getClassTime());
		LocalTime end = start.plusMinutes(dto.getDurationMins());

		if (!classesRepo.findConflictingByRoom(room.getFacilityId(), start, end).isEmpty())
			throw new BusinessRuleException("Room is already booked at this time slot.");
		if (!classesRepo.findConflictingByTrainer(trainer.getTrainerId(), start, end).isEmpty())
			throw new BusinessRuleException("Trainer is already assigned to another class at this time.");

		Classes cls = mapper.map(dto, Classes.class);
		cls.setTrainer(trainer);
		cls.setRoom(room);
		cls.setBranch(branch);
		cls.setStatus(Classes.Status.ACTIVE);
		return mapper.map(classesRepo.save(cls), ClassesDTO.class);
	}

	public List<ClassesDTO> getAllClasses() {
		return classesRepo.findAll().stream().map(c -> mapper.map(c, ClassesDTO.class)).collect(Collectors.toList());
	}

	public List<ClassesDTO> getClassesByBranch(Long branchId) {
		return classesRepo.findByBranchBranchId(branchId).stream().map(c -> mapper.map(c, ClassesDTO.class))
				.collect(Collectors.toList());
	}

	public ClassesDTO getClassById(Long id) {
		return mapper.map(findById(id), ClassesDTO.class);
	}

	public ClassesDTO updateClass(Long id, ClassesDTO dto) {
		Classes cls = findById(id);
		mapper.map(dto, cls);
		return mapper.map(classesRepo.save(cls), ClassesDTO.class);
	}

	public void cancelClass(Long id, String reason) {
		Classes cls = findById(id);
		if (reason == null || reason.isBlank())
			throw new BusinessRuleException("Cancel reason is required.");
		cls.setStatus(Classes.Status.CANCELLED);
		cls.setCancelReason(reason);
		classesRepo.save(cls);
	}

	private Classes findById(Long id) {
		return classesRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
	}
}