package com.fitness.service;

import com.fitness.dto.PromoCodeDTO;
import com.fitness.entity.PromoCode;
import com.fitness.entity.AuditLog.Action;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

	private final PromoCodeRepository promoRepo;
	private final com.fitness.repository.PromoCodeUsageRepository promoUsageRepo;
	private final com.fitness.repository.MemberRepository memberRepo;
	private final AuditLogService auditLogService;
	private final ModelMapper mapper;

	public PromoCodeDTO createPromoCode(PromoCodeDTO dto) {
		if (promoRepo.existsByCode(dto.getCode()))
			throw new DuplicateResourceException("PromoCode", "code", dto.getCode());
		PromoCode promo = mapper.map(dto, PromoCode.class);
		promo.setIsActive(true);
		PromoCode saved = promoRepo.save(promo);
		auditLogService.logForCurrentUser("PromoCode", saved.getPromoId(), Action.CREATE,
			"Promo code created: " + saved.getCode(), null);
		return mapper.map(saved, PromoCodeDTO.class);
	}

	public PromoCodeDTO validateAndGet(String code, Long memberId) {
		PromoCode promo = promoRepo.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "code", code));
		if (!promo.getIsActive())
			throw new BusinessRuleException("Promo code is inactive.");
		if (LocalDate.parse(promo.getExpiryDate().toString()).isBefore(LocalDate.now()))
			throw new BusinessRuleException("Promo code has expired.");
			
		// 1. Enforce Global Usage Limit
		long totalUsage = promoUsageRepo.countByPromoCodePromoId(promo.getPromoId());
		if (promo.getUsageLimit() > 0 && totalUsage >= promo.getUsageLimit()) {
			throw new BusinessRuleException("Promo code usage limit exceeded.");
		}
		
		if (memberId != null) {
			// 2. Enforce Per Member Limit
			long memberUsage = promoUsageRepo.countByPromoCodePromoIdAndMemberMemberId(promo.getPromoId(), memberId);
			if (promo.getPerMemberLimit() > 0 && memberUsage >= promo.getPerMemberLimit()) {
				throw new BusinessRuleException("You have exceeded the usage limit for this promo code.");
			}
			
			// 3. Eligibility Rules
			if (promo.getEligibility() == PromoCode.Eligibility.NEW) {
				com.fitness.entity.Member member = memberRepo.findById(memberId)
					.orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));
				if (member.getStatus() != com.fitness.entity.Member.Status.PROSPECT) {
					throw new BusinessRuleException("This promo code is only valid for new members.");
				}
			}
		}
			
		return mapper.map(promo, PromoCodeDTO.class);
	}

	public List<PromoCodeDTO> getAllPromoCodes() {
		return promoRepo.findAll().stream().map(p -> mapper.map(p, PromoCodeDTO.class)).collect(Collectors.toList());
	}

	public void deactivatePromoCode(Long id) {
		PromoCode promo = promoRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "id", id));
		promo.setIsActive(false);
		promoRepo.save(promo);
		auditLogService.logForCurrentUser("PromoCode", id, Action.UPDATE, "isActive=true", "isActive=false");
	}

	public byte[] exportPromoUsageCsv() {
		List<com.fitness.entity.PromoCodeUsage> usages = promoUsageRepo.findAll();
		StringBuilder sb = new StringBuilder();
		sb.append("UsageID,PromoCode,MemberID,MemberName,DiscountApplied,UsedAt\n");
		for (com.fitness.entity.PromoCodeUsage u : usages) {
			String promoCodeStr = u.getPromoCode() != null ? u.getPromoCode().getCode() : "";
			String memName = u.getMember() != null ? u.getMember().getMemName() : "";
			Long memId = u.getMember() != null ? u.getMember().getMemberId() : null;
			sb.append(String.format("%d,%s,%d,%s,%.2f,%s\n",
				u.getId(), promoCodeStr, memId, memName,
				0.00, u.getUsedAt() != null ? u.getUsedAt().toString() : ""));
		}
		return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}
}