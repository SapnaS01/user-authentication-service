package com.self.userauth.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	/*
	 * This is used to convert once class object to another class object
	 */

	@Bean
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}

}
