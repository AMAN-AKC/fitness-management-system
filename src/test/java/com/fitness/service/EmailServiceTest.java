package com.fitness.service;

import com.fitness.dto.ReceiptDTO;
import com.fitness.entity.Classes;
import com.fitness.entity.Member;
import com.fitness.entity.Receipt;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@test.com");
    }

    @Test
    void sendReceiptEmail_Success() {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setMemberEmail("member@test.com");
        dto.setReceiptNumber("REC-123");
        dto.setTotalAmount(BigDecimal.TEN);
        dto.setMemberName("John Doe");

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        boolean result = emailService.sendReceiptEmail(dto);

        assertTrue(result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendDunningNotification_Success() {
        Member member = new Member();
        member.setEmail("member@test.com");
        member.setMemName("John Doe");

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        boolean result = emailService.sendDunningNotification(member, "INV-123", BigDecimal.TEN);

        assertTrue(result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendRegistrationWelcomeEmail_Success() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        boolean result = emailService.sendRegistrationWelcomeEmail("member@test.com", "John Doe", "john", "temp123", "WELCOME");

        assertTrue(result);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
