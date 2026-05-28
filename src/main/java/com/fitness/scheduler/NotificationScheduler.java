package com.fitness.scheduler;

import com.fitness.entity.Membership;
import com.fitness.entity.Notification;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.SystemUserRepository;
import com.fitness.service.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

	private final MembershipRepository membershipRepo;
	private final ApplicationEventPublisher eventPublisher;

	// Runs daily at 9:00 AM
	@Scheduled(cron = "0 0 9 * * *")
	public void sendRenewalReminders() {
		log.info("Starting Daily Renewal Reminder Job");
		// Find memberships expiring in exactly 5 days
		LocalDate targetDate = LocalDate.now().plusDays(5);
		List<Membership> expiringMemberships = membershipRepo.findByEndDateAndStatus(targetDate, Membership.Status.ACTIVE);

		for (Membership membership : expiringMemberships) {
			Map<String, Object> vars = new HashMap<>();
			vars.put("memberName", membership.getMember().getMemName());
			vars.put("planName", membership.getPlan().getPlanName());
			vars.put("expiryDate", membership.getEndDate().toString());

			NotificationEvent event = new NotificationEvent(
					this,
					membership.getMember().getUser(),
					Notification.NotifType.RENEWAL,
					"Renewal Reminder",
					vars,
					"/member/dashboard", // deep link
					"Your membership expires in 5 days.",
					"Renew now to avoid interruption."
			);
			eventPublisher.publishEvent(event);
		}
	}

	// Runs daily at 6:00 PM for daily digest
	@Scheduled(cron = "0 0 18 * * *")
	public void sendDailyDigest() {
		log.info("Starting Daily Digest Job");
		// We would find users with daily digest preference and send a compilation
		// Stubbed for brevity. Real implementation queries preferences and aggregates stats.
	}

	// Runs every Sunday at 6:00 PM
	@Scheduled(cron = "0 0 18 * * SUN")
	public void sendWeeklyDigest() {
		log.info("Starting Weekly Digest Job");
	}
}
