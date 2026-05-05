package com.fitness.service;

import com.fitness.dto.PtSessionDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PtSessionService {

	private final PtSessionRepository ptRepo;
	private final MemberRepository memberRepo;
	private final TrainerRepository trainerRepo;
	private final ModelMapper mapper;

	public PtSessionDTO requestSession(PtSessionDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Trainer trainer = trainerRepo.findById(dto.getTrainerId())
				.orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", dto.getTrainerId()));

		LocalDateTime start = LocalDateTime.parse(dto.getScheduledAt());
		LocalDateTime end = start.plusMinutes(dto.getDurationMins());

		if (!ptRepo.findOverlappingForTrainer(trainer.getTrainerId(), start, end).isEmpty())
			throw new BusinessRuleException("Trainer already has a session at this time.");

		PtSession session = mapper.map(dto, PtSession.class);
		session.setMember(member);
		session.setTrainer(trainer);
		session.setStatus(PtSession.Status.REQUESTED);
		return mapper.map(ptRepo.save(session), PtSessionDTO.class);
	}

	public PtSessionDTO updateStatus(Long id, PtSession.Status status, String notes) {
		PtSession session = ptRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PtSession", "id", id));
		session.setStatus(status);
		if (notes != null)
			session.setTrainerNotes(notes);
		return mapper.map(ptRepo.save(session), PtSessionDTO.class);
	}

	public List<PtSessionDTO> getSessionsByMember(Long memberId) {
		return ptRepo.findByMemberMemberId(memberId).stream().map(s -> mapper.map(s, PtSessionDTO.class))
				.collect(Collectors.toList());
	}

	public List<PtSessionDTO> getSessionsByTrainer(Long trainerId) {
		return ptRepo.findByTrainerTrainerId(trainerId).stream().map(s -> mapper.map(s, PtSessionDTO.class))
				.collect(Collectors.toList());
	}
}
