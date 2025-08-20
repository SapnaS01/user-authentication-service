package com.self.userauth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInter {

	private static final String OTP_LOGIN = "otp:login:";
	private static final String REGISTER_SESSION = "register-session:";
	private static final String OTP_REGISTER = "otp:register:";

	private final PhonesRepository phonesRepository;
	private final OtpService otpService;
	private final OtpCacheRepository otpCacheRepository;
	private final UserRepository userRepository;
	private final Map<String, Map<String, Object>> otpInMemoryCache = new ConcurrentHashMap<>();
	private final JwtService jwtService;


	@Override
	public AuthResponse signUp(String phone) {
		log.info("Sign-up request received for phone: {}", maskPhone(phone));
		if (phonesRepository.existsByPhone(phone)) {
			log.warn("Sign-up failed: phone {} already registered", maskPhone(phone));
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
//		TODO: add caching mechanism
		otpInMemoryCache.put(OTP_REGISTER + phone, otpData);

		log.info("OTP generated for phone {} and expires at {}", maskPhone(phone), expiresAt);


		return new AuthResponse(true, "OTP sent successfully", null);
	}

	@Override
	public AuthResponse verifyOtp(String phone,String otp) {
		log.info("OTP verification attempt for phone {}", maskPhone(phone));
		Map<String, Object> otpData = otpInMemoryCache.get(OTP_REGISTER + phone);
		if (otpData == null) {
			log.warn("OTP verification failed: No OTP found for phone {}", maskPhone(phone));
			throw new BadRequestException("OTP not found or expired");
		}
		String cachedOtp = (String) otpData.get("otp");
		LocalDateTime expiresAt = (LocalDateTime) otpData.get("expiresAt");

		if (LocalDateTime.now().isAfter(expiresAt)) {
			otpInMemoryCache.remove(OTP_REGISTER + phone);
			log.warn("OTP expired for phone {}", maskPhone(phone));
			throw new BadRequestException("OTP has expired");
		}

		if (!cachedOtp.equals(otp)) {
			log.warn("Invalid OTP entered for phone {}", maskPhone(phone));
			throw new BadRequestException("Invalid OTP");
		}


		// Remove OTP after verification
		otpInMemoryCache.remove(OTP_REGISTER + phone);

		// Generate temporary token
		String tempToken = java.util.UUID.randomUUID().toString();

		//		save the phone number as verified in the cache for future 
		Map<String, Object> verifiedData = new HashMap<>();
		verifiedData.put("phone", phone);
		verifiedData.put("isVerified", true);
		verifiedData.put("verifiedAt", LocalDateTime.now());  

		otpInMemoryCache.put(REGISTER_SESSION + tempToken, verifiedData);
		log.info("OTP verified successfully for phone {}. Temp token generated.", maskPhone(phone));

		return new AuthResponse(true, "OTP verified successfully", tempToken);

	}



	/**
	 * @Transactional: multi-step DB operations
	 * If we don’t use @Transactional here, the entity becomes detached after save(), 
	 * and Hibernate will not track new refresh tokens unless we explicitly merge or save again.
	 * */
	@Transactional
	@Override
	public AuthResponse completeRegistration(String firstName, String lastName, String email, String tempToken) {
		log.info("Completing registration for tempToken {}", tempToken);
		Map<String, Object> verifiedData = otpInMemoryCache.get(REGISTER_SESSION + tempToken);
		if (verifiedData == null || !(Boolean) verifiedData.getOrDefault("isVerified", false)) {
			log.warn("Registration failed: Invalid or expired tempToken {}", tempToken);
			throw new BadRequestException("Phone number not verified or session expired");
		}

		String phoneNumber = (String) verifiedData.get("phone");

		Phones phone = Phones.builder()
				.phone(phoneNumber)
				.isPrimary(true)
				.build();

		User user = User.builder()
				.firstName(firstName)
				.lastName(lastName)
				.phone(phone)
				.build();

		phone.setUser(user);

		if (email != null && !email.trim().isEmpty()) {
			Emails emailEntity = Emails.builder()
					.email(email)
					.isPrimary(true)
					.user(user)
					.build();
			//	        user.setEmails(List.of(emailEntity));  //List.of will create an immutable list 
			List<Emails> emailList = new ArrayList<>();
			emailList.add(emailEntity);
			user.setEmails(emailList);

		}

		// persist user to get ID 
		userRepository.save(user);  

		String accessToken = jwtService.generateAccessToken(user);
		String refreshTokenValue = jwtService.generateRefreshToken(user);

		//add the refresh token
		RefreshTokens refreshToken = RefreshTokens.builder()
				.token(refreshTokenValue)
				.expiresAt(LocalDateTime.now().plusDays(30))
				.revoked(false)
				.user(user)
				.build();

		// Save again ( persist refresh token)
		user.addRefreshToken(refreshToken);
		//	    userRepository.save(user);  // no need as we used transactional 

		otpInMemoryCache.remove(REGISTER_SESSION + tempToken);

		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put("accessToken", accessToken);
		tokenMap.put("refreshToken", refreshTokenValue);
		log.info("User registered successfully with phone {}", maskPhone(phoneNumber));

		return new AuthResponse(true, "User registered successfully", tokenMap);
	}

	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 4) return "****";
		return "****" + phone.substring(phone.length() - 4);
	}

	@Override
	public AuthResponse login(String phone) {
		log.info("Login attempt for phone {}", maskPhone(phone));
		// check if phone exists
		if (!phonesRepository.existsByPhone(phone)) {
			log.warn("Login failed: phone {} not registered", maskPhone(phone));
			throw new BadRequestException("User not registered with this phone number");
		}
		// Generate OTP for login
		String otp = otpService.generateOtp();

		// TODO: otpService.sendOtp(phone, otp);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		OtpCache otpCache = OtpCache.builder()
				.phone(phone)
				.otp(otp)
				.action(OtpAction.LOGIN)
				.expiresAt(expiresAt)
				.build();

		otpCacheRepository.save(otpCache);


		Map<String, Object> otpData = new HashMap<>();
		otpData.put("otp", otp);
		otpData.put("expiresAt", expiresAt);
		otpInMemoryCache.put(OTP_LOGIN + phone, otpData);

		log.info("OTP generated for phone {} and expires at {}", maskPhone(phone), expiresAt);


		return new AuthResponse(true, "OTP sent successfully", null);

	}

	@Override
	public AuthResponse verifyLoginOtp(String phone, String otp) {
		log.info("OTP verification attempt for phone {}", maskPhone(phone));
		Map<String, Object> otpData = otpInMemoryCache.get(OTP_LOGIN + phone);
		if (otpData == null) {
			log.warn("OTP verification failed: No OTP found for phone {}", maskPhone(phone));
			throw new BadRequestException("OTP not found or expired");
		}
		String cachedOtp = (String) otpData.get("otp");
		LocalDateTime expiresAt = (LocalDateTime) otpData.get("expiresAt");

		if (LocalDateTime.now().isAfter(expiresAt)) {
			otpInMemoryCache.remove(OTP_LOGIN + phone);
			log.warn("OTP expired for phone {}", maskPhone(phone));
			throw new BadRequestException("OTP has expired");
		}

		if (!cachedOtp.equals(otp)) {
			log.warn("Invalid OTP entered for phone {}", maskPhone(phone));
			throw new BadRequestException("Invalid OTP");
		}


		// remove OTP after verification
		otpInMemoryCache.remove(OTP_LOGIN + phone);

		//  fetch user via Optional, no null checks needed
		User user = phonesRepository.findByPhone(phone)
				.map(Phones::getUser) // Extract User from Phones (method reference)
				.orElseThrow(() -> new BadRequestException("User not found for given phone"));

		// generate tokens
		String accessToken = jwtService.generateAccessToken(user);
		String refreshTokenValue = jwtService.generateRefreshToken(user);

		// build and attach refresh token
		RefreshTokens refreshToken = RefreshTokens.builder()
				.token(refreshTokenValue)
				.expiresAt(LocalDateTime.now().plusDays(30))
				.revoked(false)
				.user(user)
				.build();

		user.addRefreshToken(refreshToken);
		userRepository.save(user);
		log.info("Login successful for phone {}. Tokens generated.", maskPhone(phone));

		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put("accessToken", accessToken);
		tokenMap.put("refreshToken", refreshTokenValue);

		return new AuthResponse(true, "Login successful", tokenMap);

	}


}
