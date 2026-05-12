package com.fitness.service;

import com.fitness.entity.Member;
import com.fitness.entity.Receipt;
import com.fitness.dto.ReceiptDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailService {

	@Value("${spring.mail.enabled:false}")
	private boolean emailEnabled;

	@Value("${spring.mail.from:no-reply@fitness.local}")
	private String fromEmail;

	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	/**
	 * Send receipt email to member
	 * Stub implementation - ready for actual mail service integration
	 * (JavaMailSender)
	 */
	public boolean sendReceiptEmail(Receipt receipt) {
		if (!emailEnabled) {
			return false; // Email disabled in configuration
		}

		Member member = receipt.getMember();
		String toEmail = member.getEmail();
		String receiptNumber = receipt.getReceiptNumber();
		BigDecimal amount = receipt.getTotalAmount();
		String planName = receipt.getInvoice().getMembership() != null
				? receipt.getInvoice().getMembership().getPlan().getPlanName()
				: "N/A";

		String subject = "Payment Receipt - " + receiptNumber;
		String body = buildReceiptEmailBody(member.getMemName(), receiptNumber, amount, planName,
				receipt.getPayment().getPaymentDate().toString());

		return sendTextEmail(toEmail, subject, body);
	}

	/**
	 * Build formatted receipt email body
	 */
	private String buildReceiptEmailBody(String memberName, String receiptNumber,
			BigDecimal amount, String planName, String paymentDate) {
		return "Dear " + memberName + ",\n\n" + "Your payment has been processed successfully.\n\n"
				+ "Receipt Number: " + receiptNumber + "\n" + "Plan: " + planName + "\n"
				+ "Amount: ₹ " + amount + "\n" + "Payment Date: " + paymentDate + "\n\n"
				+ "Thank you for your membership!\n\n" + "Best regards,\n" + "Fitness Management Team";
	}

	/**
	 * Send dunning notification email
	 * Stub implementation
	 */
	public boolean sendDunningNotification(Member member, String invoiceNumber,
			BigDecimal outstandingAmount) {
		if (!emailEnabled) {
			return false;
		}

		String subject = "Payment Reminder - Outstanding Balance";
		String body = "Dear " + member.getMemName() + ",\n\n"
				+ "You have an outstanding payment for invoice " + invoiceNumber + "\n"
				+ "Amount Due: ₹ " + outstandingAmount + "\n\n"
				+ "Please make payment at your earliest convenience.\n\n" + "Best regards,\n"
				+ "Fitness Management Team";

		System.out.println("[EMAIL SERVICE] Dunning notice sent to " + member.getEmail()
				+ " | Subject: " + subject);

		return true;
	}

	public boolean sendRenewalReminder(Member member, String planName, String expiryDate) {
		if (!emailEnabled) {
			return false;
		}

		String subject = "Membership Renewal Reminder";
		String body = "Dear " + member.getMemName() + ",\n\n"
				+ "Your membership for " + planName + " expires on " + expiryDate + "\n\n"
				+ "Renew now to avoid service interruption.\n\n" + "Best regards,\n"
				+ "Fitness Management Team";

		return sendTextEmail(member.getEmail(), subject, body);
	}

	public boolean sendReceiptEmail(ReceiptDTO receipt) {
		if (!emailEnabled) {
			return false;
		}

		String toEmail = receipt.getMemberEmail();
		String receiptNumber = receipt.getReceiptNumber();
		BigDecimal amount = receipt.getTotalAmount();
		String planName = receipt.getPlanName() != null ? receipt.getPlanName() : "N/A";
		String body = buildReceiptEmailBody(receipt.getMemberName(), receiptNumber, amount, planName,
				receipt.getPaymentDate() != null ? receipt.getPaymentDate().toString() : "");

		return sendTextEmail(toEmail, "Payment Receipt - " + receiptNumber, body);
	}

	private boolean sendTextEmail(String toEmail, String subject, String body) {
		if (!emailEnabled) {
			return false;
		}

		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender != null) {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(fromEmail);
				message.setTo(toEmail);
				message.setSubject(subject);
				message.setText(body);
				mailSender.send(message);
				return true;
			} catch (Exception ex) {
				System.out.println("[EMAIL SERVICE] Mail send failed, falling back to console: " + ex.getMessage());
			}
		}

		System.out.println("[EMAIL SERVICE] Email prepared for " + toEmail + " | Subject: " + subject);
		System.out.println(body);
		return true;
	}
}
