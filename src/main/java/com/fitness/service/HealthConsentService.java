package com.fitness.service;

import com.fitness.dto.HealthConsentDTO;
import com.fitness.entity.*;
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
public class HealthConsentService {

	private final HealthConsentRepository consentRepo;
	private final MemberRepository memberRepo;
	private final ModelMapper mapper;

	public HealthConsentDTO submitConsent(HealthConsentDTO dto, String ipAddress) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		// Expire any existing active consent
		consentRepo.findByMemberMemberIdAndStatus(member.getMemberId(), HealthConsent.Status.ACTIVE)
				.ifPresent(old -> {
					old.setStatus(HealthConsent.Status.EXPIRED);
					consentRepo.save(old);
				});

		HealthConsent consent = mapper.map(dto, HealthConsent.class);
		consent.setMember(member);
		consent.setAcknowledgedAt(LocalDateTime.now());
		consent.setIpAddress(ipAddress);
		consent.setStatus(HealthConsent.Status.ACTIVE);
		return mapper.map(consentRepo.save(consent), HealthConsentDTO.class);
	}

	public List<HealthConsentDTO> getConsentsByMember(Long memberId) {
		return consentRepo.findByMemberMemberId(memberId).stream()
				.map(c -> mapper.map(c, HealthConsentDTO.class)).collect(Collectors.toList());
	}

	public boolean hasActiveConsent(Long memberId) {
		return consentRepo.existsByMemberMemberIdAndStatus(memberId, HealthConsent.Status.ACTIVE);
	}
}
