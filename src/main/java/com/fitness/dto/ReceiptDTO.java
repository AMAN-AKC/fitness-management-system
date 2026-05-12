package com.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDTO {
	private Long receiptId;
	private String receiptNumber;
	private Long invoiceId;
	private String invoiceNumber;
	private Long memberId;
	private String memberName;
	private String memberEmail;
	private String planName;
	private BigDecimal amount;
	private BigDecimal taxAmount;
	private BigDecimal totalAmount;
	private String paymentMethod;
	private LocalDateTime paymentDate;
	private String status; // ISSUED, EMAILED, PRINTED
	private String notes;
	private LocalDateTime createdAt;
}
