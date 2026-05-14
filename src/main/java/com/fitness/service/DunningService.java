package com.fitness.service;

import com.fitness.entity.Invoice;
import com.fitness.entity.Membership;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import com.fitness.entity.AuditLog;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DunningService {

	private final InvoiceRepository invoiceRepo;
	private final MembershipRepository membershipRepo;
	private final AuditLogService auditLogService;

	/**
	 * Handle failed payment - update invoice and membership status to PENDING
	 */
	public void handleFailedPayment(Long invoiceId, String failureReason) {
		Invoice invoice = invoiceRepo.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

		// Mark invoice as overdue/pending
		invoice.setStatus(Invoice.Status.OVERDUE);
		invoiceRepo.save(invoice);

		// Update associated membership to PENDING if exists
		if (invoice.getMembership() != null) {
			Membership membership = invoice.getMembership();
			membership.setStatus(Membership.Status.PENDING);
			membershipRepo.save(membership);

			// Audit log
			auditLogService.logForCurrentUser("Membership", membership.getMemId(), AuditLog.Action.UPDATE,
					null, "Status changed to PENDING due to failed payment: " + failureReason);
		}
	}

	/**
	 * Transition to DUNNING status if payment remains overdue
	 * Stub: Called after defined grace period (e.g., 3 days)
	 */
	public void transitionToDunning(Long invoiceId) {
		Invoice invoice = invoiceRepo.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

		if (invoice.getStatus() != Invoice.Status.OVERDUE) {
			return;
		}

		invoice.setStatus(Invoice.Status.OVERDUE); // Could add DUNNING status if needed
		invoiceRepo.save(invoice);

		if (invoice.getMembership() != null) {
			Membership membership = invoice.getMembership();
			membership.setStatus(Membership.Status.DUNNING);
			membershipRepo.save(membership);

			auditLogService.logForCurrentUser("Membership", membership.getMemId(), AuditLog.Action.UPDATE,
					null, "Status changed to DUNNING - payment overdue");
		}
	}

	/**
	 * Resolve dunning status when payment is received
	 */
	public void resolveDunning(Long membershipId) {
		Membership membership = membershipRepo.findById(membershipId)
				.orElseThrow(() -> new ResourceNotFoundException("Membership", "id", membershipId));

		if (membership.getStatus() == Membership.Status.DUNNING
				|| membership.getStatus() == Membership.Status.PENDING) {
			membership.setStatus(Membership.Status.ACTIVE);
			membershipRepo.save(membership);

			auditLogService.logForCurrentUser("Membership", membershipId, AuditLog.Action.UPDATE,
					null, "Status changed to ACTIVE - dunning resolved");
		}
	}

	/**
	 * Get all invoices in dunning/overdue state
	 */
	public List<Invoice> getOverdueInvoices() {
		return invoiceRepo.findByStatus(Invoice.Status.OVERDUE);
	}

	/**
	 * Get invoices overdue for more than specified days
	 */
	public List<Invoice> getInvoicesOverdueByDays(int days) {
		LocalDate threshold = LocalDate.now().minusDays(days);
		return invoiceRepo.findByStatus(Invoice.Status.OVERDUE).stream()
				.filter(inv -> inv.getCreatedAt().toLocalDate().isBefore(threshold))
				.collect(Collectors.toList());
	}

	/**
	 * Get dunning memberships
	 */
	public List<Membership> getDunningMemberships() {
		return membershipRepo.findByStatus(Membership.Status.DUNNING);
	}

	/**
	 * Suspend membership if dunning continues (stub for automation)
	 */
	public void suspendDunningMembership(Long membershipId, String reason) {
		Membership membership = membershipRepo.findById(membershipId)
				.orElseThrow(() -> new ResourceNotFoundException("Membership", "id", membershipId));

		if (membership.getStatus() == Membership.Status.DUNNING) {
			membership.setStatus(Membership.Status.SUSPENDED);
			membershipRepo.save(membership);

			auditLogService.logForCurrentUser("Membership", membershipId, AuditLog.Action.UPDATE,
					null, "Status changed to SUSPENDED - " + reason);
		}
	}
}
