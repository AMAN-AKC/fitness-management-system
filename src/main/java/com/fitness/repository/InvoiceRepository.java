package com.fitness.repository;

import com.fitness.entity.Invoice;
import com.fitness.entity.Invoice.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

	List<Invoice> findByMemberMemberId(Long memberId);

	List<Invoice> findByStatus(Status status);

	boolean existsByInvoiceNumber(String invoiceNumber);

	boolean existsByMembershipMemIdAndStatusIn(Long membershipId, List<Status> statuses);

	@org.springframework.data.jpa.repository.Query("SELECT SUM(i.finalAmount) FROM Invoice i WHERE i.status = 'PAID'")
	java.math.BigDecimal sumTotalRevenue();

	@org.springframework.data.jpa.repository.Query("SELECT SUM(i.finalAmount) FROM Invoice i WHERE i.status = 'PAID' AND i.createdAt BETWEEN ?1 AND ?2")
	java.math.BigDecimal sumRevenueByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

	List<Invoice> findByStatusIn(List<Status> statuses);
}