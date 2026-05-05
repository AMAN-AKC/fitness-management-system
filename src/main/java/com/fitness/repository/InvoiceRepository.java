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
}