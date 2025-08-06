package com.self.userauth.mapper;

import org.springframework.stereotype.Component;

import com.self.userauth.dto.SignUpRquestDto;
import com.self.userauth.pojo.SignUpRequest;

@Component
public class UserMapper {
	public SignUpRquestDto toDto(SignUpRequest request) {
		SignUpRquestDto dto = new SignUpRquestDto();
		dto.setPhoneNumber(request.getPhone());
		return dto;
	}
}
