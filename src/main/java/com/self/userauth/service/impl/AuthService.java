package com.self.userauth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.userauth.dto.LoginDto;
import com.self.userauth.dto.RegistrationDto;
import com.self.userauth.dto.SignUpDto;
import com.self.userauth.dto.VerifyOtpDto;
import com.self.userauth.exception.BadRequestException;
import com.self.userauth.model.Emails;
import com.self.userauth.model.OtpCache;
import com.self.userauth.model.Phones;
import com.self.userauth.model.RefreshTokens;
import com.self.userauth.model.User;
import com.self.userauth.model.enums.OtpAction;
import com.self.userauth.pojo.AuthResponse;
import com.self.userauth.pojo.OtpData;
import com.self.userauth.repository.OtpCacheRepository;
import com.self.userauth.repository.PhonesRepository;
import com.self.userauth.repository.UserRepository;
import com.self.userauth.service.inter.AuthServiceInter;
import com.self.userauth.util.RedisHelper;

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
	private final RedisHelper redisHelper;
	private final ObjectMapper objectMapper;

	//	in-memory on a single JVM.
	private final Map<String, Map<String, Object>> otpInMemoryCache = new ConcurrentHashMap<>();
	private final JwtService jwtService;


	@Override
	public AuthResponse signUp(SignUpDto dto) {
		log.info("Sign-up request received for phone: {}", maskPhone(dto.getPhone()));
		if (phonesRepository.existsByPhone(dto.getPhone())) {
			log.warn("Sign-up failed: phone {} already registered", maskPhone(dto.getPhone()));
			throw new BadRequestException("User already registered with this phone number");
		}
		String otp = otpService.generateOtp();

		// TODO: otpService.sendOtp(phone, otp);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		OtpCache otpCache = OtpCache.builder()
				.phone(dto.getPhone())
				.otp(otp)
				.action(OtpAction.REGISTER)
				.expiresAt(expiresAt)
				.build();

		otpCacheRepository.save(otpCache);


		//		Map<String, Object> otpData = new HashMap<>();
		//		otpData.put("otp", otp);
		//		otpData.put("expiresAt", expiresAt);

		//		otpInMemoryCache.put(OTP_REGISTER + phone, otpData);	
		redisHelper.setWithTtl(OTP_REGISTER + dto.getPhone(), new OtpData(otp, expiresAt), 5, TimeUnit.MINUTES);

		log.info("OTP generated for phone {} and expires at {}", maskPhone(dto.getPhone()), expiresAt);

		return new AuthResponse(true, "OTP sent successfully", null);
	}

	@Override
	public AuthResponse verifyOtp(VerifyOtpDto dto) {
		log.info("OTP verification attempt for phone {}", maskPhone(dto.getPhone()));
		//		Map<String, Object> otpData = otpInMemoryCache.get(OTP_REGISTER + phone);
		//		if (otpData == null) {
		//			log.warn("OTP verification failed: No OTP found for phone {}", maskPhone(phone));
		//			throw new BadRequestException("OTP not found or expired");
		//		}
		//		String cachedOtp = (String) otpData.get("otp");
		//		LocalDateTime expiresAt = (LocalDateTime) otpData.get("expiresAt");

		// Fetch OTP data from Redis
		OtpData otpData = redisHelper.get(OTP_REGISTER + dto.getPhone(), OtpData.class);
		if (otpData == null) {
			throw new BadRequestException("OTP not found or expired");
		}

		// Expiration check
		if (LocalDateTime.now().isAfter(otpData.getExpiresAt())) {
			redisHelper.delete(OTP_REGISTER + dto.getPhone());
			throw new BadRequestException("OTP has expired");
		}

		// Validate OTP
		if (!otpData.getOtp().equals(dto.getOtp())) {
			throw new BadRequestException("Invalid OTP");
		}

		// Remove OTP after successful verification
		redisHelper.delete(OTP_REGISTER + dto.getPhone());

		// Generate temp token
		String tempToken = UUID.randomUUID().toString();

		// Store verification info with TTL
		Map<String, Object> verifiedData = new HashMap<>();
		verifiedData.put("phone", dto.getPhone());
		verifiedData.put("isVerified", true);
		verifiedData.put("verifiedAt", LocalDateTime.now());

		redisHelper.setWithTtl(REGISTER_SESSION + tempToken, verifiedData, 5, TimeUnit.MINUTES);

		return new AuthResponse(true, "OTP verified successfully", tempToken);
	}





	/**
	 * @Transactional: multi-step DB operations
	 * If we don’t use @Transactional here, the entity becomes detached after save(), 
	 * and Hibernate will not track new refresh tokens unless we explicitly merge or save again.
	 * */

	@Transactional
	@Override
	public AuthResponse completeRegistration(RegistrationDto dto) {
		log.info("Completing registration for tempToken {}", dto.getTempToken());

		Object obj = redisHelper.get(REGISTER_SESSION + dto.getTempToken(), Object.class);
		Map<String, Object> verifiedData = objectMapper.convertValue(
				obj,
				new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
				);

		if (verifiedData == null || !(Boolean) verifiedData.getOrDefault("isVerified", false)) {
			log.warn("Registration failed: Invalid or expired tempToken {}", dto.getTempToken());
			throw new BadRequestException("Phone number not verified or session expired");
		}

		String phoneNumber = (String) verifiedData.get("phone");

		Phones phone = Phones.builder()
				.phone(phoneNumber)
				.isPrimary(true)
				.build();

		User user = User.builder()
				.firstName(dto.getFirstName())
				.lastName(dto.getLastName())
				.phone(phone)
				.build();
		phone.setUser(user);

		if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
			Emails emailEntity = Emails.builder()
					.email(dto.getEmail())
					.isPrimary(true)
					.user(user)
					.build();
			List<Emails> emailList = new ArrayList<>();
			emailList.add(emailEntity);
			user.setEmails(emailList);
		}

		// Persist user
		userRepository.save(user);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshTokenValue = jwtService.generateRefreshToken(user);

		RefreshTokens refreshToken = RefreshTokens.builder()
				.token(refreshTokenValue)
				.expiresAt(LocalDateTime.now().plusDays(30))
				.revoked(false)
				.user(user)
				.build();
		user.addRefreshToken(refreshToken);

		redisHelper.delete(REGISTER_SESSION + dto.getTempToken());

		// Build response data
		Map<String, Object> responseData = new HashMap<>();
		Map<String, Object> userData = new HashMap<>();
		userData.put("id", user.getId());
		userData.put("phone", phoneNumber);
		userData.put("firstName", user.getFirstName());
		userData.put("lastName", user.getLastName());
		userData.put("email", dto.getEmail());
		responseData.put("tokens", Map.of(
				"accessToken", accessToken,
				"refreshToken", refreshTokenValue));


		responseData.put("user", userData);

		log.info("User registered successfully with phone {}", maskPhone(phoneNumber));
		return new AuthResponse(true, "User registered successfully", responseData);
	}


	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 4) return "****";
		return "****" + phone.substring(phone.length() - 4);
	}

	@Override
	public AuthResponse login(LoginDto dto) {
		log.info("Login attempt for phone {}", maskPhone(dto.getPhone()));
		// check if phone exists
		if (!phonesRepository.existsByPhone(dto.getPhone())) {
			log.warn("Login failed: phone {} not registered", maskPhone(dto.getPhone()));
			throw new BadRequestException("User not registered with this phone number");
		}
		// Generate OTP for login
		String otp = otpService.generateOtp();

		// TODO: otpService.sendOtp(phone, otp);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
		OtpCache otpCache = OtpCache.builder()
				.phone(dto.getPhone())
				.otp(otp)
				.action(OtpAction.LOGIN)
				.expiresAt(expiresAt)
				.build();

		otpCacheRepository.save(otpCache);


		//		Map<String, Object> otpData = new HashMap<>();
		//		otpData.put("otp", otp);
		//		otpData.put("expiresAt", expiresAt);
		//		otpInMemoryCache.put(OTP_LOGIN + phone, otpData);
		redisHelper.setWithTtl(OTP_LOGIN + dto.getPhone(), new OtpData(otp, expiresAt), 5, TimeUnit.MINUTES);

		log.info("OTP generated for phone {} and expires at {}", maskPhone(dto.getPhone()), expiresAt);


		return new AuthResponse(true, "OTP sent successfully", null);

	}

	@Override
	public AuthResponse verifyLoginOtp(VerifyOtpDto dto) {
		log.info("OTP verification attempt for phone {}", maskPhone(dto.getPhone()));
		//		Map<String, Object> otpData = otpInMemoryCache.get(OTP_LOGIN + phone);
		OtpData otpData = redisHelper.get(OTP_LOGIN + dto.getPhone(), OtpData.class);
		if (otpData == null) {
			log.warn("OTP verification failed: No OTP found for phone {}", maskPhone(dto.getPhone()));
			throw new BadRequestException("OTP not found or expired");
		}
		String cachedOtp = otpData.getOtp();
		LocalDateTime expiresAt = otpData.getExpiresAt();

		if (LocalDateTime.now().isAfter(expiresAt)) {
			//			otpInMemoryCache.remove(OTP_LOGIN + phone);
			redisHelper.delete(OTP_LOGIN + dto.getPhone());
			log.warn("OTP expired for phone {}", maskPhone(dto.getPhone()));
			throw new BadRequestException("OTP has expired");
		}

		if (!cachedOtp.equals(dto.getOtp())) {
			log.warn("Invalid OTP entered for phone {}", maskPhone(dto.getPhone()));
			throw new BadRequestException("Invalid OTP");
		}


		// remove OTP after verification
		//		otpInMemoryCache.remove(OTP_LOGIN + phone);
		redisHelper.delete(OTP_LOGIN + dto.getPhone());

		//  fetch user via Optional, no null checks needed
		User user = phonesRepository.findByPhone(dto.getPhone())
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
		log.info("Login successful for phone {}. Tokens generated.", maskPhone(dto.getPhone()));

		Map<String, Object> responseData = new HashMap<>();
		Map<String, Object> userData = new HashMap<>();
		userData.put("id", user.getId());
		userData.put("phone", user.getPhone().getPhone());
		userData.put("firstName", user.getFirstName());
		userData.put("lastName", user.getLastName());
		userData.put("email", user.getEmails().isEmpty() ? null : user.getEmails().get(0).getEmail());
		responseData.put("tokens", Map.of(
				"accessToken", accessToken,
				"refreshToken", refreshTokenValue));


		responseData.put("user", userData);
		return new AuthResponse(true, "Login successful", responseData);

	}


}
