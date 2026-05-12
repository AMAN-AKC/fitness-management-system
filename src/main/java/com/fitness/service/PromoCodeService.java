package com.fitness.service;

import com.fitness.dto.PromoCodeDTO;
import com.fitness.entity.PromoCode;
import com.fitness.entity.AuditLog.Action;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.PromoCodeRepository;
import com.fitness.repository.PromoCodeUsageRepository;
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
	private final PromoCodeUsageRepository promoUsageRepo;
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

	public PromoCodeDTO validateAndGet(String code) {
		PromoCode promo = promoRepo.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("PromoCode", "code", code));
		if (!promo.getIsActive())
			throw new BusinessRuleException("Promo code is inactive.");
		if (LocalDate.parse(promo.getExpiryDate().toString()).isBefore(LocalDate.now()))
			throw new BusinessRuleException("Promo code has expired.");
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
}