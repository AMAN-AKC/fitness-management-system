package com.fitness.service;

import com.fitness.dto.ReceiptDTO;
import com.fitness.entity.Classes;
import com.fitness.entity.EmailTemplate;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.entity.Payment;
import com.fitness.entity.Plan;
import com.fitness.entity.Receipt;
import com.fitness.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private EmailTemplateRepository templateRepo;

    @Mock
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@test.local");
        lenient().when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
    }

    @Test
    void sendReceiptEmail_Entity_Success() {
        Member member = new Member();
        member.setEmail("test@test.com");
        member.setMemName("Test Name");

        Plan plan = new Plan();
        plan.setPlanName("Basic");
        Membership membership = new Membership();
        membership.setPlan(plan);
        Invoice invoice = new Invoice();
        invoice.setMembership(membership);
        
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.now());
        
        Receipt receipt = new Receipt();
        receipt.setMember(member);
        receipt.setReceiptNumber("RCT-123");
        receipt.setTotalAmount(new BigDecimal("100.00"));
        receipt.setInvoice(invoice);
        receipt.setPayment(payment);

        boolean result = emailService.sendReceiptEmail(receipt);

        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDunningNotification_Success() {
        Member member = new Member();
        member.setEmail("test@test.com");
        member.setMemName("Test Name");

        boolean result = emailService.sendDunningNotification(member, "INV-123", new BigDecimal("50.00"));

        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendRenewalReminder_Success() {
        Member member = new Member();
        member.setEmail("test@test.com");
        member.setMemName("Test Name");

        boolean result = emailService.sendRenewalReminder(member, "Basic", "2023-12-31");

        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendReceiptEmail_DTO_Success() {
        ReceiptDTO receipt = new ReceiptDTO();
        receipt.setMemberEmail("test@test.com");
        receipt.setReceiptNumber("RCT-123");
        receipt.setTotalAmount(new BigDecimal("100.00"));

        boolean result = emailService.sendReceiptEmail(receipt);

        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        boolean result = emailService.sendPasswordResetEmail("test@test.com", "123456");
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendRegistrationWelcomeEmail_Success() {
        boolean result = emailService.sendRegistrationWelcomeEmail("test@test.com", "Name", "user1", "pass", "CODE");
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmail_Success() {
        boolean result = emailService.sendVerificationEmail("test@test.com", "token");
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBookingConfirmationEmail_Success() {
        Member member = new Member();
        member.setEmail("test@test.com");
        Classes cls = new Classes();
        cls.setClassName("Yoga");
        cls.setStartDate(LocalDate.now());

        boolean result = emailService.sendBookingConfirmationEmail(member, cls, "CONFIRMED");
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBookingCancellationEmail_Success() {
        Member member = new Member();
        member.setEmail("test@test.com");
        Classes cls = new Classes();
        cls.setClassName("Yoga");
        cls.setStartDate(LocalDate.now());

        boolean result = emailService.sendBookingCancellationEmail(member, cls, true);
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDynamicEmail_WithTemplate_Success() {
        EmailTemplate template = new EmailTemplate();
        template.setSubject("Hello {{name}}");
        template.setBodyHtml("Welcome {{name}}");
        
        when(templateRepo.findByTemplateName("welcome")).thenReturn(Optional.of(template));
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");

        boolean result = emailService.sendDynamicEmail("test@test.com", "welcome", variables, "fallback", "fallback");
        
        assertTrue(result);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
