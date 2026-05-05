package com.fitness.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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

		return mapper;
	}
}