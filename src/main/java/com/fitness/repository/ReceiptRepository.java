package com.fitness.repository;

import com.fitness.entity.Receipt;
import com.fitness.entity.Receipt.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
	Optional<Receipt> findByReceiptNumber(String receiptNumber);

	List<Receipt> findByMemberMemberId(Long memberId);

	List<Receipt> findByStatus(Status status);

	List<Receipt> findByInvoiceInvoiceId(Long invoiceId);

	boolean existsByReceiptNumber(String receiptNumber);
}
