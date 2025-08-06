package com.self.userauth.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.self.userauth.exception.BadRequestException;
import com.self.userauth.model.OtpCache;
import com.self.userauth.model.enums.OtpAction;
import com.self.userauth.pojo.AuthResponse;
import com.self.userauth.repository.OtpCacheRepository;
import com.self.userauth.repository.PhonesRepository;
import com.self.userauth.service.inter.AuthServiceInter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInter {

	private final PhonesRepository phonesRepository;
	private final OtpService otpService;
	//	private final RedisTemplate<String, String> redisTemplate;
	private final OtpCacheRepository otpCacheRepository;
	private final Map<String, Map<String, Object>> otpInMemoryCache = new ConcurrentHashMap<>();


	@Override
	public AuthResponse signUp(String phone) {
		if (phonesRepository.existsByPhone(phone)) {
			throw new BadRequestException("User already registered with this phone number");
		}
		String otp = otpService.generateOtp();

		// otpService.sendOtp(phone, otp);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

		OtpCache otpCache = OtpCache.builder()
				.phone(phone)
				.otp(otp)
				.action(OtpAction.REGISTER)
				.expiresAt(expiresAt)
				.build();

		otpCacheRepository.save(otpCache);

		//       Save OTP to Redis with expiry
		//        redisTemplate.opsForValue().set("otp:register:" + phone, otp, Duration.ofMinutes(5));
		//        in order to avoid using redis setup, we can use a map to store the OTP temporarily

		// Save in in-memory map (acting like Redis)
		Map<String, Object> otpData = new HashMap<>();
		otpData.put("otp", otp);
		otpData.put("expiresAt", expiresAt);
		otpInMemoryCache.put("otp:register:" + phone, otpData);

		return new AuthResponse(true, "OTP sent successfully", null);
	}

	@Override
	public AuthResponse verifyOtp(String phone,String otp) {
		Map<String, Object> otpData = otpInMemoryCache.get("otp:register:" + phone);
		if (otpData == null) {
			throw new BadRequestException("OTP not found or expired");
		}
		String cachedOtp = (String) otpData.get("otp");
		LocalDateTime expiresAt = (LocalDateTime) otpData.get("expiresAt");

		if (LocalDateTime.now().isAfter(expiresAt)) {
			otpInMemoryCache.remove("otp:register:" + phone);
			throw new BadRequestException("OTP has expired");
		}

		if (!cachedOtp.equals(otp)) {
			throw new BadRequestException("Invalid OTP");
		}
		
		
		// Remove OTP after successful verification
		otpInMemoryCache.remove("otp:register:" + phone);
		
//		// Save the details to the database		
		
		return new AuthResponse(true, "OTP verified successfully", null);


	}

}
