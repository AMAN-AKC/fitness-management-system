package com.fitness.repository;

import com.fitness.entity.Payment;
import com.fitness.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByMemberMemberId(Long memberId);

	List<Payment> findByInvoiceInvoiceId(Long invoiceId);

	List<Payment> findByPaymentStatus(PaymentStatus status);
}