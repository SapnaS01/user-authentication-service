package com.self.userauth.service.inter;

import com.self.userauth.pojo.AuthResponse;

public interface AuthServiceInter {
	
	AuthResponse signUp(String phone);
	AuthResponse verifyOtp(String phone,String otp);

}
