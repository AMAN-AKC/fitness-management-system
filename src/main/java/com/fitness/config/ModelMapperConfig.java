package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.modelmapper.Converter;
import org.modelmapper.convention.MatchingStrategies;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fitness.entity.Attendance;
import com.fitness.dto.AttendanceDTO;
import com.fitness.entity.ClassBooking;
import com.fitness.dto.ClassBookingDTO;
import com.fitness.entity.Notification;
import com.fitness.dto.NotificationDTO;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();

		// STRICT matching prevents accidental field mis-mapping
		// (e.g. memberId mapped to memId in a different DTO)
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setSkipNullEnabled(true) // skip null source fields on update
				.setFieldMatchingEnabled(true)
				.setFieldAccessLevel(
						org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

		// String -> LocalDate
		Converter<String, LocalDate> toStringDate = ctx -> ctx.getSource() == null ? null
				: LocalDate.parse(ctx.getSource(), DateTimeFormatter.ISO_LOCAL_DATE);

		// LocalDate -> String
		Converter<LocalDate, String> fromLocalDate = ctx -> ctx.getSource() == null ? null
				: ctx.getSource().format(DateTimeFormatter.ISO_LOCAL_DATE);

		// String -> LocalTime
		Converter<String, java.time.LocalTime> toStringTime = ctx -> {
			if (ctx.getSource() == null) return null;
			String val = ctx.getSource().trim();
			if (val.length() == 5) {
				return java.time.LocalTime.parse(val, DateTimeFormatter.ofPattern("HH:mm"));
			} else if (val.length() == 8) {
				return java.time.LocalTime.parse(val, DateTimeFormatter.ofPattern("HH:mm:ss"));
			}
			return java.time.LocalTime.parse(val);
		};

		// LocalTime -> String
		Converter<java.time.LocalTime, String> fromLocalTime = ctx -> ctx.getSource() == null ? null
				: ctx.getSource().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

		// String -> LocalDateTime
		Converter<String, java.time.LocalDateTime> toStringDateTime = ctx -> ctx.getSource() == null ? null
				: java.time.LocalDateTime.parse(ctx.getSource(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		// LocalDateTime -> String
		Converter<java.time.LocalDateTime, String> fromLocalDateTime = ctx -> ctx.getSource() == null ? null
				: ctx.getSource().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		mapper.addConverter(toStringDate);
		mapper.addConverter(fromLocalDate);
		mapper.addConverter(toStringTime);
		mapper.addConverter(fromLocalTime);
		mapper.addConverter(toStringDateTime);
		mapper.addConverter(fromLocalDateTime);

		mapper.typeMap(Attendance.class, AttendanceDTO.class).addMappings(m -> {
			m.map(src -> src.getMember().getMemberId(), AttendanceDTO::setMemberId);
			m.map(src -> src.getBranch().getBranchId(), AttendanceDTO::setBranchId);
			m.map(src -> src.getFitnessClass() != null ? src.getFitnessClass().getClassId() : null, AttendanceDTO::setClassId);
			m.map(src -> src.getOverrideBy() != null ? src.getOverrideBy().getUserId() : null, AttendanceDTO::setOverrideBy);
		});

		mapper.typeMap(ClassBooking.class, ClassBookingDTO.class).addMappings(m -> {
			m.map(src -> src.getFitnessClass().getClassId(), ClassBookingDTO::setClassId);
			m.map(src -> src.getMember().getMemberId(), ClassBookingDTO::setMemberId);
			m.map(src -> src.getOverrideBy() != null ? src.getOverrideBy().getUserId() : null, ClassBookingDTO::setOverrideBy);
		});

		mapper.typeMap(Notification.class, NotificationDTO.class).addMappings(m -> {
			m.map(src -> src.getUser().getUserId(), NotificationDTO::setUserId);
		});

		return mapper;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule()); // Support for Java 8 date/time types
		return mapper;
	}
}