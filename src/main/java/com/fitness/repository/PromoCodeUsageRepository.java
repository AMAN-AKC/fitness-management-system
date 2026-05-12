package com.fitness.repository;

import com.fitness.entity.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {
	List<PromoCodeUsage> findByPromoCodePromoId(Long promoId);

	List<PromoCodeUsage> findByMemberMemberId(Long memberId);

	long countByPromoCodePromoIdAndMemberMemberId(Long promoId, Long memberId);

	long countByPromoCodePromoId(Long promoId);
}
