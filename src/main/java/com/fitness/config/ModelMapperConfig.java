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
import com.fitness.entity.SystemUser;
import com.fitness.entity.Classes;
import com.fitness.entity.Member;
import com.fitness.entity.Branch;

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

		Converter<SystemUser, Long> userToId = ctx -> ctx.getSource() == null ? null : ctx.getSource().getUserId();
		Converter<Classes, Long> classToId = ctx -> ctx.getSource() == null ? null : ctx.getSource().getClassId();
		Converter<Member, Long> memberToId = ctx -> ctx.getSource() == null ? null : ctx.getSource().getMemberId();
		Converter<Branch, Long> branchToId = ctx -> ctx.getSource() == null ? null : ctx.getSource().getBranchId();
		Converter<com.fitness.entity.Plan, Long> planToId = ctx -> ctx.getSource() == null ? null : ctx.getSource().getPlanId();

		mapper.typeMap(Attendance.class, AttendanceDTO.class).addMappings(m -> {
			m.using(memberToId).map(Attendance::getMember, AttendanceDTO::setMemberId);
			m.using(branchToId).map(Attendance::getBranch, AttendanceDTO::setBranchId);
			m.using(classToId).map(Attendance::getFitnessClass, AttendanceDTO::setClassId);
			m.using(userToId).map(Attendance::getOverrideBy, AttendanceDTO::setOverrideBy);
		});

		mapper.typeMap(ClassBooking.class, ClassBookingDTO.class).addMappings(m -> {
			m.using(classToId).map(ClassBooking::getFitnessClass, ClassBookingDTO::setClassId);
			m.using(memberToId).map(ClassBooking::getMember, ClassBookingDTO::setMemberId);
			m.using(userToId).map(ClassBooking::getOverrideBy, ClassBookingDTO::setOverrideBy);
		});
		
		mapper.typeMap(com.fitness.entity.Membership.class, com.fitness.dto.MembershipDTO.class).addMappings(m -> {
			m.using(memberToId).map(com.fitness.entity.Membership::getMember, com.fitness.dto.MembershipDTO::setMemberId);
			m.using(planToId).map(com.fitness.entity.Membership::getPlan, com.fitness.dto.MembershipDTO::setPlanId);
			m.using(branchToId).map(com.fitness.entity.Membership::getBranch, com.fitness.dto.MembershipDTO::setBranchId);
		});

		mapper.typeMap(Member.class, com.fitness.dto.MemberDTO.class).addMappings(m -> {
			m.using(branchToId).map(Member::getHomeBranch, com.fitness.dto.MemberDTO::setHomeBranchId);
		});

		mapper.typeMap(SystemUser.class, com.fitness.dto.SystemUserDTO.class).addMappings(m -> {
			m.map(SystemUser::isActive, com.fitness.dto.SystemUserDTO::setIsActive);
			m.map(SystemUser::getIsLocked, com.fitness.dto.SystemUserDTO::setIsLocked);
			m.using(branchToId).map(SystemUser::getBranch, com.fitness.dto.SystemUserDTO::setBranchId);
		});

		mapper.typeMap(com.fitness.dto.SystemUserDTO.class, SystemUser.class).addMappings(m -> {
			m.map(com.fitness.dto.SystemUserDTO::getIsActive, SystemUser::setActive);
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