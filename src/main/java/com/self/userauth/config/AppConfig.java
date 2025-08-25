package com.self.userauth.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class AppConfig {

	/*
	 * This is used to convert once class object to another class object
	 */

	@Bean
	ModelMapper getModelMapper() {
		return new ModelMapper();
	}

	/**
	 * ObjectMapper bean used by Jackson for JSON serialization/deserialization.
	 * RedisHelper and RedisTemplate will use this to convert objects to JSON and back.
	 */
	@Bean
	ObjectMapper objectMapper() {
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.registerModule(new JavaTimeModule());
	    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    return mapper;
	}

}
