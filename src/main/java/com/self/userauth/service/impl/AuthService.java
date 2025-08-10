package com.self.userauth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.self.userauth.exception.BadRequestException;
import com.self.userauth.model.Emails;
import com.self.userauth.model.OtpCache;
import com.self.userauth.model.Phones;
import com.self.userauth.model.RefreshTokens;
import com.self.userauth.model.User;
import com.self.userauth.model.enums.OtpAction;
import com.self.userauth.pojo.AuthResponse;
import com.self.userauth.repository.OtpCacheRepository;
import com.self.userauth.repository.PhonesRepository;
import com.self.userauth.repository.UserRepository;
import com.self.userauth.service.inter.AuthServiceInter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInter {

	private final PhonesRepository phonesRepository;
	private final OtpService otpService;
	private final OtpCacheRepository otpCacheRepository;
	private final UserRepository userRepository;
	private final Map<String, Map<String, Object>> otpInMemoryCache = new ConcurrentHashMap<>();


	@Override
	public AuthResponse signUp(String phone) {
		if (phonesRepository.existsByPhone(phone)) {
			throw new BadRequestException("User already registered with this phone number");
		}
		String otp = otpService.generateOtp();

		// TODO: otpService.sendOtp(phone, otp);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		OtpCache otpCache = OtpCache.builder()
				.phone(phone)
				.otp(otp)
				.action(OtpAction.REGISTER)
				.expiresAt(expiresAt)
				.build();

		otpCacheRepository.save(otpCache);


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


		// Remove OTP after verification
		otpInMemoryCache.remove("otp:register:" + phone);

		// Generate temporary token
		String tempToken = java.util.UUID.randomUUID().toString();

		//		save the phone number as verified in the cache for future reference
		Map<String, Object> verifiedData = new HashMap<>();
		verifiedData.put("phone", phone);
		verifiedData.put("isVerified", true);
		verifiedData.put("verifiedAt", LocalDateTime.now());  

		otpInMemoryCache.put("register-session:" + tempToken, verifiedData);

		return new AuthResponse(true, "OTP verified successfully", tempToken);

	}

	@Override
	public AuthResponse completeRegistration(String firstName, String lastName, String email, String tempToken) {
	    Map<String, Object> verifiedData = otpInMemoryCache.get("register-session:" + tempToken);

	    if (verifiedData == null || !(Boolean) verifiedData.getOrDefault("isVerified", false)) {
	        throw new BadRequestException("Phone number not verified or session expired");
	    }

	    String phoneNumber = (String) verifiedData.get("phone");

	    // Create phone entity
	    Phones phone = Phones.builder()
	            .phone(phoneNumber)
	            .isPrimary(true)
	            .build();

	    // Create user
	    User.UserBuilder userBuilder = User.builder()
	            .firstName(firstName)
	            .lastName(lastName)
	            .phone(phone);

	    List<Emails> emailsList = new ArrayList<>();

	    // Check for email and create email entity only if it's not null or blank
	    if (email != null && !email.trim().isEmpty()) {
	        Emails emailEntity = Emails.builder()
	                .email(email)
	                .isPrimary(true)
	                .build();
	        emailsList.add(emailEntity);
	        emailEntity.setUser(null); // will be set after user is built
	    }

	    User user = userBuilder.emails(emailsList.isEmpty() ? null : emailsList).build();

	    // Set bidirectional mappings
	    phone.setUser(user);
	    emailsList.forEach(e -> e.setUser(user));

	    // Generate tokens
	    String accessToken = UUID.randomUUID().toString();
	    String refreshTokenValue = UUID.randomUUID().toString();

	    RefreshTokens refreshToken = RefreshTokens.builder()
	            .token(refreshTokenValue)
	            .expiresAt(LocalDateTime.now().plusDays(30))
	            .revoked(false)
	            .user(user)
	            .build();

	    user.setRefreshTokens(List.of(refreshToken));

	    // Save to DB
	    userRepository.save(user);

	    // Remove from cache
	    otpInMemoryCache.remove("register-session:" + tempToken);

	    Map<String, String> tokenMap = new HashMap<>();
	    tokenMap.put("accessToken", accessToken);
	    tokenMap.put("refreshToken", refreshTokenValue);

	    return new AuthResponse(true, "User registered successfully", tokenMap);
	}

}
