package com.self.userauth.service.impl;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.self.userauth.service.inter.OtpServiceInerface;

@Service
public class OtpService implements OtpServiceInerface {
	private static final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        int otp = 1000 + secureRandom.nextInt(9000);
        return String.valueOf(otp);
    }

}
