package com.fitness.dto;

import com.fitness.entity.ClassBooking;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassBookingDTO {
	private Long bookingId;

	@NotNull(message = "Please provide a valid class")
	private Long classId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	private ClassBooking.BookingStatus bookingStatus;
	private Integer waitlistPosition;
	private String cancelledAt;
	private Long overrideBy;
	private String overrideReason;
}