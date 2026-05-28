package com.fitness.service;

import com.fitness.entity.AuditLog;
import com.fitness.entity.Invoice;
import com.fitness.entity.Membership;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipExpiryTask {

	private final MembershipRepository membershipRepo;
	private final InvoiceRepository invoiceRepo;
	private final AuditLogService auditLogService;

	@Scheduled(cron = "0 0 2 * * ?") // Run every day at 2:00 AM
	@Transactional
	public void expirePendingMemberships() {
		log.info("Starting Pending Membership Expiry Task...");
		
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(15);
		
		List<Membership> pendingMemberships = membershipRepo.findByStatus(Membership.Status.PENDING);
		
		int expiredCount = 0;
		for (Membership m : pendingMemberships) {
			if (m.getCreatedAt().isBefore(cutoffDate)) {
				m.setStatus(Membership.Status.EXPIRED);
				membershipRepo.save(m);
				
				// Void the associated issued invoice
				List<Invoice> invoices = invoiceRepo.findByMembershipMemId(m.getMemId());
				for (Invoice inv : invoices) {
					if (inv.getStatus() == Invoice.Status.ISSUED || inv.getStatus() == Invoice.Status.PENDING) {
						inv.setStatus(Invoice.Status.VOID);
						invoiceRepo.save(inv);
					}
				}
				
				// System Audit log
				auditLogService.logForSystem("Membership", m.getMemId(), AuditLog.Action.UPDATE, 
					"{\"status\":\"PENDING\"}", "{\"status\":\"CANCELLED\"} - Automated 15-day expiry");
					
				expiredCount++;
			}
		}
		
		log.info("Finished Pending Membership Expiry Task. Expired {} memberships.", expiredCount);
	}
}
