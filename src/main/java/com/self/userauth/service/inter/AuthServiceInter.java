package com.self.userauth.service.inter;

import com.self.userauth.dto.LoginDto;
import com.self.userauth.dto.RegistrationDto;
import com.self.userauth.dto.SignUpDto;
import com.self.userauth.dto.VerifyOtpDto;
import com.self.userauth.pojo.AuthResponse;

public interface AuthServiceInter {
	
	AuthResponse signUp(SignUpDto dto);
	AuthResponse verifyOtp(VerifyOtpDto dto);
	AuthResponse completeRegistration(RegistrationDto dto);
	AuthResponse login(LoginDto dto);
	AuthResponse verifyLoginOtp(VerifyOtpDto dto);

}
