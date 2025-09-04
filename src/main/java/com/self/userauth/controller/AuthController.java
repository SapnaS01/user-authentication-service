package com.self.userauth.controller;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.self.userauth.dto.LoginDto;
import com.self.userauth.dto.RegistrationDto;
import com.self.userauth.dto.SignUpDto;
import com.self.userauth.dto.VerifyOtpDto;
import com.self.userauth.pojo.AuthResponse;
import com.self.userauth.pojo.LoginRequest;
import com.self.userauth.pojo.RegistrationRequest;
import com.self.userauth.pojo.SignUpRequest;
import com.self.userauth.pojo.VerifyOtpRequest;
import com.self.userauth.service.inter.AuthServiceInter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthServiceInter authService;
	private final ModelMapper modelMapper;

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
		SignUpDto dto = modelMapper.map(signUpRequest, SignUpDto.class);
		AuthResponse response = authService.signUp(dto);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<AuthResponse> verifyOtp( @RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
		VerifyOtpDto dto = modelMapper.map(verifyOtpRequest, VerifyOtpDto.class);
		AuthResponse response = authService.verifyOtp(dto);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/complete-registration")
	public ResponseEntity<AuthResponse> completeRegistration(
			@RequestHeader("X-Temp-Token") String tempToken,
			@RequestBody @Valid RegistrationRequest registrationRequest) {

		RegistrationDto dto = modelMapper.map(registrationRequest, RegistrationDto.class);
		dto.setTempToken(tempToken); 
		AuthResponse response = authService.completeRegistration(dto);

		@SuppressWarnings("unchecked")
		Map<String, Object> meta = (Map<String, Object>) response.getData();

		// Extract tokens map
		@SuppressWarnings("unchecked")
		Map<String, String> tokens = (Map<String, String>) meta.get("tokens");
		String accessToken = tokens.get("accessToken");
		String refreshToken = tokens.get("refreshToken");

		return ResponseEntity.ok()
				.header("X-Access-Token", accessToken)
				.header("X-Refresh-Token", refreshToken)
				.body(new AuthResponse(true, "User registered successfully", meta.get("user"))); 
	}


	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
		LoginDto dto = modelMapper.map(loginRequest, LoginDto.class);
		AuthResponse response = authService.login(dto);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-login-otp")
	public ResponseEntity<AuthResponse> verifyLoginOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
		VerifyOtpDto dto = modelMapper.map(verifyOtpRequest, VerifyOtpDto.class);
		AuthResponse response = authService.verifyLoginOtp(dto);

		@SuppressWarnings("unchecked")
		Map<String, Object> meta = (Map<String, Object>) response.getData();
		
		// Extract tokens map
		@SuppressWarnings("unchecked")
		Map<String, String> tokens = (Map<String, String>) meta.get("tokens");

		// Get individual tokens
		String accessToken = tokens.get("accessToken");
		String refreshToken = tokens.get("refreshToken");
		System.out.println("Access Token: " + accessToken);

		return ResponseEntity.ok()
				.header("X-Access-Token", accessToken)
				.header("X-Refresh-Token", refreshToken)
				.body(new AuthResponse(true, "Login successful", meta.get("user")));
	}
}
