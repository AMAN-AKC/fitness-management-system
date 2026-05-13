package com.fitness.service;

import com.fitness.dto.HealthConsentDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthConsentService {

	private final HealthConsentRepository consentRepo;
	private final MemberRepository memberRepo;
	private final SystemUserRepository userRepo;
	private final AuditLogService auditLogService;

	@Value("${consent.current-version:v1.0}")
	private String currentVersion;

	@Value("${consent.validity-days:365}")
	private int consentValidityDays;

	@Value("${consent.retention-days:2555}")
	private int retentionDays;

	public HealthConsentDTO submitConsent(HealthConsentDTO dto, String ipAddress) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		ensureMemberCanAccess(member.getMemberId());
		if (!Boolean.TRUE.equals(dto.getMedicalAcknowledged())
				|| !Boolean.TRUE.equals(dto.getLiabilityAcknowledged())
				|| !Boolean.TRUE.equals(dto.getPrivacyAcknowledged())) {
			throw new BusinessRuleException("All required health and waiver acknowledgements must be accepted.");
		}
		// Expire any existing active consent
		consentRepo.findByMemberMemberIdAndStatus(member.getMemberId(), HealthConsent.Status.ACTIVE)
				.ifPresent(old -> {
					old.setStatus(HealthConsent.Status.EXPIRED);
					consentRepo.save(old);
					auditLogService.logForCurrentUser("HealthConsent", old.getConsentId(), AuditLog.Action.UPDATE,
							"status=ACTIVE", "status=EXPIRED");
				});

		HealthConsent consent = HealthConsent.builder()
				.member(member)
				.formVersion(currentVersion)
				.parqResponses(dto.getParqResponses())
				.medicalAcknowledged(dto.getMedicalAcknowledged())
				.liabilityAcknowledged(dto.getLiabilityAcknowledged())
				.privacyAcknowledged(dto.getPrivacyAcknowledged())
				.acknowledgedAt(LocalDateTime.now())
				.expiresAt(LocalDateTime.now().plusDays(consentValidityDays))
				.ipAddress(ipAddress)
				.status(HealthConsent.Status.ACTIVE)
				.build();
		consent.setMember(member);
		HealthConsent saved = consentRepo.save(consent);
		auditLogService.logForCurrentUser("HealthConsent", saved.getConsentId(), AuditLog.Action.CREATE,
				null, "memberId=" + member.getMemberId() + ", formVersion=" + saved.getFormVersion());
		return toDto(saved, false);
	}

	public List<HealthConsentDTO> getConsentsByMember(Long memberId) {
		ensureMemberCanAccess(memberId);
		boolean maskSensitive = isStaffOrTrainer();
		return consentRepo.findByMemberMemberIdOrderByAcknowledgedAtDesc(memberId).stream()
				.map(c -> toDto(c, maskSensitive)).collect(Collectors.toList());
	}

	public boolean hasActiveConsent(Long memberId) {
		return getConsentStatus(memberId).get("consentRequired").equals(Boolean.FALSE);
	}

	public Map<String, Object> getConsentStatus(Long memberId) {
		HealthConsent latest = consentRepo.findTopByMemberMemberIdOrderByAcknowledgedAtDesc(memberId).orElse(null);
		boolean active = latest != null
				&& latest.getStatus() == HealthConsent.Status.ACTIVE
				&& currentVersion.equals(latest.getFormVersion())
				&& (latest.getExpiresAt() == null || latest.getExpiresAt().isAfter(LocalDateTime.now()));
		Map<String, Object> status = new LinkedHashMap<>();
		status.put("memberId", memberId);
		status.put("currentVersion", currentVersion);
		status.put("consentRequired", !active);
		status.put("requiresReconfirmation", latest != null && !currentVersion.equals(latest.getFormVersion()));
		status.put("latestConsent", latest == null ? null : toDto(latest, isStaffOrTrainer()));
		return status;
	}

	public HealthConsentDTO addAdministrativeNote(Long consentId, String note) {
		if (note == null || note.isBlank()) {
			throw new BusinessRuleException("Administrative note is required.");
		}
		String lowered = note.toLowerCase();
		if (lowered.contains("diagnos") || lowered.contains("prescrib") || lowered.contains("treat")) {
			throw new BusinessRuleException("Staff notes must be administrative and non-diagnostic.");
		}
		HealthConsent consent = consentRepo.findById(consentId)
				.orElseThrow(() -> new ResourceNotFoundException("HealthConsent", "id", consentId));
		String old = consent.getStaffNotes();
		consent.setStaffNotes(note.trim());
		HealthConsent saved = consentRepo.save(consent);
		auditLogService.logForCurrentUser("HealthConsent", consentId, AuditLog.Action.UPDATE,
				"staffNotes=" + old, "staffNotes=" + saved.getStaffNotes());
		return toDto(saved, true);
	}

	public byte[] downloadConsentHistoryPdf(Long memberId) {
		ensureMemberCanAccess(memberId);
		List<HealthConsentDTO> consents = getConsentsByMember(memberId);
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Document document = new Document();
			PdfWriter.getInstance(document, out);
			document.open();
			document.add(new Paragraph("Health Consent History"));
			document.add(new Paragraph("Member ID: " + memberId));
			for (HealthConsentDTO consent : consents) {
				document.add(new Paragraph("Consent #" + consent.getConsentId()
						+ " | version " + consent.getFormVersion()
						+ " | status " + consent.getStatus()
						+ " | acknowledged " + consent.getAcknowledgedAt()
						+ " | expires " + consent.getExpiresAt()));
			}
			document.close();
			auditLogService.logForCurrentUser("HealthConsent", memberId, AuditLog.Action.UPDATE,
					null, "Downloaded consent history");
			return out.toByteArray();
		} catch (Exception e) {
			throw new BusinessRuleException("Unable to generate consent history PDF: " + e.getMessage());
		}
	}

	public List<Map<String, Object>> exportAnonymizedHealthStats() {
		return consentRepo.summarizeAnonymizedStats().stream().map(row -> {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("formVersion", row[0]);
			item.put("status", row[1]);
			item.put("consentCount", row[2]);
			item.put("positiveParqResponseCount", row[3]);
			return item;
		}).collect(Collectors.toList());
	}

	public Map<String, Object> getRetentionPolicy() {
		return Map.of("currentVersion", currentVersion,
				"validityDays", consentValidityDays,
				"retentionDays", retentionDays);
	}

	public int deleteExpiredByRetentionPolicy() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
		List<HealthConsent> expired = consentRepo.findByAcknowledgedAtBefore(cutoff);
		expired.forEach(c -> auditLogService.logForCurrentUser("HealthConsent", c.getConsentId(),
				AuditLog.Action.DELETE, "retentionDays=" + retentionDays, "deletedByRetentionPolicy=true"));
		consentRepo.deleteAll(expired);
		return expired.size();
	}

	public Member getCurrentMember() {
		SystemUser user = currentUser();
		return memberRepo.findByUserUserId(user.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "userId", user.getUserId()));
	}

	private HealthConsentDTO toDto(HealthConsent consent, boolean maskSensitive) {
		return HealthConsentDTO.builder()
				.consentId(consent.getConsentId())
				.memberId(consent.getMember().getMemberId())
				.formVersion(consent.getFormVersion())
				.parqResponses(maskSensitive ? mask(consent.getParqResponses()) : consent.getParqResponses())
				.medicalAcknowledged(consent.getMedicalAcknowledged())
				.liabilityAcknowledged(consent.getLiabilityAcknowledged())
				.privacyAcknowledged(consent.getPrivacyAcknowledged())
				.acknowledgedAt(consent.getAcknowledgedAt() != null ? consent.getAcknowledgedAt().toString() : null)
				.expiresAt(consent.getExpiresAt() != null ? consent.getExpiresAt().toString() : null)
				.ipAddress(maskSensitive ? mask(consent.getIpAddress()) : consent.getIpAddress())
				.status(consent.getStatus())
				.staffNotes(consent.getStaffNotes())
				.requiresReconfirmation(!currentVersion.equals(consent.getFormVersion()))
				.consentRequired(consent.getStatus() != HealthConsent.Status.ACTIVE
						|| !currentVersion.equals(consent.getFormVersion())
						|| (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())))
				.build();
	}

	private String mask(String value) {
		return value == null || value.isBlank() ? value : "****";
	}

	private void ensureMemberCanAccess(Long memberId) {
		SystemUser user = currentUser();
		if (user.getRole().name().equals("MEMBER")) {
			Member member = memberRepo.findByUserUserId(user.getUserId())
					.orElseThrow(() -> new ResourceNotFoundException("Member", "userId", user.getUserId()));
			if (!member.getMemberId().equals(memberId)) {
				throw new BusinessRuleException("Members can access only their own consent records.");
			}
		}
	}

	private boolean isStaffOrTrainer() {
		SystemUser user = currentUser();
		return !user.getRole().name().equals("MEMBER") && !user.getRole().name().equals("ADMIN");
	}

	private SystemUser currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null) {
			return userRepo.findById(1L)
					.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", 1L));
		}
		return userRepo.findByUsername(auth.getName())
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "username", auth.getName()));
	}
}
