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
	private final NotificationService notificationService;
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
		
		PtSession saved = ptRepo.save(session);

		// Notify trainer of the request
		try {
			Long userId = trainer.getUser().getUserId();
			notificationService.sendNotification(userId, Notification.NotifType.BOOKING,
					Notification.Channel.IN_APP,
					"New PT Session Request",
					"Member " + member.getMemName() + " requested a session on " + saved.getScheduledAt());
		} catch (Exception ignored) {
		}

		return mapper.map(saved, PtSessionDTO.class);
	}

	public PtSessionDTO updateStatus(Long id, PtSession.Status status, String notes) {
		PtSession session = ptRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PtSession", "id", id));
		PtSession.Status oldStatus = session.getStatus();
		session.setStatus(status);
		if (notes != null)
			session.setTrainerNotes(notes);

		PtSession saved = ptRepo.save(session);

		// Handle credit mutation
		Member member = session.getMember();
		if ((status == PtSession.Status.APPROVED || status == PtSession.Status.ACCEPTED) &&
				(oldStatus != PtSession.Status.APPROVED && oldStatus != PtSession.Status.ACCEPTED && oldStatus != PtSession.Status.COMPLETED)) {
			if (member.getPtSessionCredits() != null) {
				member.setPtSessionCredits(Math.max(0, member.getPtSessionCredits() - 1));
				memberRepo.save(member);
			}
		} else if ((status == PtSession.Status.CANCELLED || status == PtSession.Status.DECLINED) &&
				(oldStatus == PtSession.Status.APPROVED || oldStatus == PtSession.Status.ACCEPTED)) {
			if (member.getPtSessionCredits() != null) {
				member.setPtSessionCredits(member.getPtSessionCredits() + 1);
				memberRepo.save(member);
			}
		}

		// Send notification to member
		try {
			Long userId = session.getMember().getUser().getUserId();
			notificationService.sendNotification(userId, Notification.NotifType.BOOKING,
					Notification.Channel.IN_APP,
					"PT Session Status Update",
					"Your PT session request with trainer " + session.getTrainer().getUser().getFullName()
							+ " has been " + status.name().toLowerCase() + ".");
		} catch (Exception ignored) {
		}

		return mapper.map(saved, PtSessionDTO.class);
	}

	public PtSessionDTO rescheduleSession(Long id, String newScheduledAt) {
		PtSession session = ptRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PtSession", "id", id));

		if (session.getStatus() == PtSession.Status.ACCEPTED) {
			if (LocalDateTime.now().isAfter(session.getScheduledAt().minusHours(24))) {
				throw new BusinessRuleException("Cannot reschedule a confirmed session within 24 hours of execution.");
			}
		}

		LocalDateTime start = LocalDateTime.parse(newScheduledAt);
		LocalDateTime end = start.plusMinutes(session.getDurationMins());

		List<PtSession> overlapping = ptRepo.findOverlappingForTrainer(session.getTrainer().getTrainerId(), start, end);
		boolean hasOverlap = overlapping.stream().anyMatch(s -> !s.getSessionId().equals(session.getSessionId()));
		if (hasOverlap) {
			throw new BusinessRuleException("Trainer already has a session at this time.");
		}

		session.setScheduledAt(start);
		session.setStatus(PtSession.Status.REQUESTED); // Reverts to requested for trainer confirmation
		PtSession saved = ptRepo.save(session);

		// Notify trainer of rescheduling
		try {
			Long userId = session.getTrainer().getUser().getUserId();
			notificationService.sendNotification(userId, Notification.NotifType.BOOKING,
					Notification.Channel.IN_APP,
					"PT Session Rescheduled",
					"Member " + session.getMember().getMemName() + " rescheduled the session to " + start);
		} catch (Exception ignored) {
		}

		return mapper.map(saved, PtSessionDTO.class);
	}

	public PtSessionDTO cancelSession(Long id) {
		PtSession session = ptRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PtSession", "id", id));

		if (session.getStatus() == PtSession.Status.ACCEPTED || session.getStatus() == PtSession.Status.APPROVED) {
			if (LocalDateTime.now().isAfter(session.getScheduledAt().minusHours(24))) {
				throw new BusinessRuleException("Cannot cancel a confirmed session within 24 hours of execution.");
			}
		}

		PtSession.Status oldStatus = session.getStatus();
		session.setStatus(PtSession.Status.CANCELLED);
		PtSession saved = ptRepo.save(session);

		// Restore package credit if it was confirmed/accepted
		if (oldStatus == PtSession.Status.ACCEPTED || oldStatus == PtSession.Status.APPROVED) {
			Member member = session.getMember();
			if (member.getPtSessionCredits() != null) {
				member.setPtSessionCredits(member.getPtSessionCredits() + 1);
				memberRepo.save(member);
			}
		}

		// Notify trainer or member
		try {
			Long trainerUserId = session.getTrainer().getUser().getUserId();
			notificationService.sendNotification(trainerUserId, Notification.NotifType.CANCELLATION,
					Notification.Channel.IN_APP,
					"PT Session Cancelled",
					"The PT session with " + session.getMember().getMemName() + " on " + session.getScheduledAt() + " has been cancelled.");
		} catch (Exception ignored) {
		}

		return mapper.map(saved, PtSessionDTO.class);
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
