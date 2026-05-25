package com.fitness.service;

import com.fitness.entity.Classes;
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

		return sendTextEmail(member.getEmail(), subject, body);
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

	public boolean sendPasswordResetEmail(String email, String otp) {
		String subject = "Password Reset OTP";
		String body = "You have requested a password reset.\n\n"
				+ "Your 6-digit OTP code is: " + otp + "\n\n"
				+ "This code will expire in 5 minutes.\n"
				+ "If you did not request this, please ignore this email.";

		return sendTextEmail(email, subject, body);
	}

	public boolean sendRegistrationWelcomeEmail(String email, String name, String username, String tempPassword, String promoCode) {
		String subject = "Welcome to Fitness Management System!";
		String body = "Dear " + name + ",\n\n"
				+ "Welcome to the Fitness Management System! Your front-desk registration is complete.\n\n"
				+ "To fully activate your membership and start booking classes, please log in and complete your plan payment.\n\n"
				+ "As a special welcome gift, use promo code: " + (promoCode != null ? promoCode : "WELCOME10") + " at checkout to receive a discount on your first plan purchase!\n\n"
				+ "Your temporary login details are:\n"
				+ "Username: " + username + "\n"
				+ "Temporary Password: " + tempPassword + "\n\n"
				+ "Please log in and change your password immediately.\n\n"
				+ "We can't wait to see you at the gym!\n\n"
				+ "Best regards,\n"
				+ "Fitness Management Team";
		return sendTextEmail(email, subject, body);
	}

	public boolean sendVerificationEmail(String email, String verificationToken) {
		String subject = "Account Verification Required";
		String body = "Thank you for registering.\n\n"
				+ "Please verify your email address using the following token:\n\n"
				+ verificationToken + "\n\n"
				+ "This code will expire in 24 hours.\n\n"
				+ "Best regards,\n"
				+ "Fitness Management Team";
		return sendTextEmail(email, subject, body);
	}

	public boolean sendBookingConfirmationEmail(Member member, Classes cls, String status) {
		String subject = "Class Booking: " + cls.getClassName();
		String body = "Dear " + member.getMemName() + ",\n\n"
				+ "Your booking status for the class '" + cls.getClassName() + "' on " 
				+ cls.getStartDate() + " at " + cls.getClassTime() + " is now: " + status + ".\n\n"
				+ "Thank you,\n"
				+ "Fitness Management Team";
		return sendTextEmail(member.getEmail(), subject, body);
	}

	public boolean sendBookingCancellationEmail(Member member, Classes cls, boolean isLateCancel) {
		String subject = "Class Cancellation: " + cls.getClassName();
		String body = "Dear " + member.getMemName() + ",\n\n"
				+ "Your booking for the class '" + cls.getClassName() + "' on " 
				+ cls.getStartDate() + " at " + cls.getClassTime() + " has been cancelled.\n"
				+ (isLateCancel ? "Note: This was a late cancellation and counts as an infraction.\n\n" : "\n")
				+ "Thank you,\n"
				+ "Fitness Management Team";
		return sendTextEmail(member.getEmail(), subject, body);
	}

	private boolean sendTextEmail(String toEmail, String subject, String body) {
		// Even if email is disabled, we simulate success by logging to console
		if (emailEnabled) {
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
		}

		System.out.println("[EMAIL SERVICE] Email prepared for " + toEmail + " | Subject: " + subject);
		System.out.println(body);
		return true;
	}

}
