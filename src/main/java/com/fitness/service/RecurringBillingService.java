package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.dto.RecurringBillingScheduleDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Invoice;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringBillingService {

	private static final List<Invoice.Status> OPEN_INVOICE_STATUSES = List.of(
			Invoice.Status.DRAFT,
			Invoice.Status.ISSUED,
			Invoice.Status.OVERDUE
	);

	private final MembershipRepository membershipRepo;
	private final InvoiceRepository invoiceRepo;
	private final InvoiceService invoiceService;
	private final EmailService emailService;
	private final AuditLogService auditLogService;

	/**
	 * Check if a plan is eligible for recurring billing
	 */
	public boolean isEligibleForRecurringBilling(Plan plan) {
		return plan.getDurationDays() >= 30;
	}

	/**
	 * Generate recurring billing schedule for a membership
	 */
	public RecurringBillingScheduleDTO createRecurringSchedule(Membership membership) {
		Plan plan = membership.getPlan();

		if (!isEligibleForRecurringBilling(plan)) {
			return RecurringBillingScheduleDTO.builder()
					.membershipId(membership.getMemId())
					.isRecurring(false)
					.reason("Plan duration less than 30 days")
					.status("NOT_ELIGIBLE")
					.build();
		}

		return RecurringBillingScheduleDTO.builder()
				.membershipId(membership.getMemId())
				.memberId(membership.getMember().getMemberId())
				.planId(plan.getPlanId())
				.isRecurring(true)
				.nextBillingDate(membership.getEndDate())
				.frequency("MONTHLY")
				.amount(plan.getPrice())
				.status("ACTIVE")
				.createdAt(LocalDate.now())
				.paymentMethod("NOT_SET")
				.build();
	}

	/**
	 * Get a membership by id.
	 */
	public Membership getMembershipById(Long membershipId) {
		return membershipRepo.findById(membershipId)
				.orElseThrow(() -> new com.fitness.exception.ResourceNotFoundException("Membership", "id", membershipId));
	}

	/**
	 * Get all memberships due for recurring billing on a specific date
	 */
	public List<Membership> getMembershipsForRecurringBilling(LocalDate billingDate) {
		return membershipRepo.findByStatus(Membership.Status.ACTIVE).stream()
				.filter(m -> m.getEndDate().isEqual(billingDate)
						|| m.getEndDate().isBefore(billingDate))
				.filter(m -> isEligibleForRecurringBilling(m.getPlan()))
				.collect(Collectors.toList());
	}

	/**
	 * Process recurring billing for memberships due on the given date.
	 */
	@Transactional
	public List<InvoiceDTO> processRecurringBilling(LocalDate billingDate) {
		return getMembershipsForRecurringBilling(billingDate).stream()
				.filter(membership -> !invoiceRepo.existsByMembershipMemIdAndStatusIn(membership.getMemId(), OPEN_INVOICE_STATUSES))
				.map(this::createRenewalInvoice)
				.collect(Collectors.toList());
	}

	@Scheduled(cron = "${billing.recurring.cron:0 0 2 * * *}")
	@Transactional
	public void runRecurringBillingJob() {
		processRecurringBilling(LocalDate.now());
	}

	private InvoiceDTO createRenewalInvoice(Membership membership) {
		BigDecimal amount = membership.getPlan().getPrice();
		BigDecimal discount = membership.getDiscountAmount() != null ? membership.getDiscountAmount() : BigDecimal.ZERO;
		BigDecimal finalAmount = amount.subtract(discount).max(BigDecimal.ZERO);

		InvoiceDTO invoiceDTO = InvoiceDTO.builder()
				.memberId(membership.getMember().getMemberId())
				.membershipId(membership.getMemId())
				.mrp(amount)
				.discount(discount)
				.taxes(BigDecimal.ZERO)
				.finalAmount(finalAmount)
				.paidAmount(BigDecimal.ZERO)
				.outstanding(finalAmount)
				.status(Invoice.Status.DRAFT)
				.build();

		InvoiceDTO createdInvoice = invoiceService.createInvoice(invoiceDTO);
		emailService.sendRenewalReminder(membership.getMember(), membership.getPlan().getPlanName(), membership.getEndDate().toString());
		auditLogService.logForCurrentUser("RecurringBilling", membership.getMemId(), AuditLog.Action.CREATE,
				null, "Renewal invoice created: " + createdInvoice.getInvoiceNumber());
		return createdInvoice;
	}

	/**
	 * Pause recurring billing (stub)
	 */
	public void pauseRecurringBilling(Long membershipId, String reason) {
		// Stub: In real implementation, pause in payment processor
	}

	/**
	 * Resume recurring billing (stub)
	 */
	public void resumeRecurringBilling(Long membershipId) {
		// Stub: In real implementation, resume in payment processor
	}
}
