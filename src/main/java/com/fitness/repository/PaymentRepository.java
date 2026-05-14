package com.fitness.repository;

import com.fitness.entity.Payment;
import com.fitness.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByMemberMemberId(Long memberId);
	List<Payment> findByInvoiceInvoiceId(Long invoiceId);
	List<Payment> findByPaymentStatus(PaymentStatus status);

	@Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.paymentStatus = com.fitness.entity.Payment.PaymentStatus.SUCCESS AND p.paymentDate >= :start")
	BigDecimal sumRevenueSince(@Param("start") LocalDateTime start);
}