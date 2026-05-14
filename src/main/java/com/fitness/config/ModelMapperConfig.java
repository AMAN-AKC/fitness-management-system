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

		mapper.addConverter(toStringDate);
		mapper.addConverter(fromLocalDate);

		return mapper;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule()); // Support for Java 8 date/time types
		return mapper;
	}
}