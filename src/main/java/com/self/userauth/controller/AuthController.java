package com.self.userauth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
		AuthResponse response = authService.signUp(signUpRequest.getPhone());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<AuthResponse> verifyOtp( @RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
		AuthResponse response = authService.verifyOtp(verifyOtpRequest.getPhone(), verifyOtpRequest.getOtp());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/complete-registration")
	public ResponseEntity<AuthResponse> completeRegistration(
			@RequestHeader("X-Temp-Token") String tempToken,
			@RequestBody @Valid RegistrationRequest registrationRequest) {

		AuthResponse response = authService.completeRegistration(registrationRequest.getFirstName(), registrationRequest.getLastName(),registrationRequest.getEmail(), tempToken);

		@SuppressWarnings("unchecked")
		Map<String, String> meta = (Map<String, String>) response.getData();
		String accessToken = meta.get("accessToken");
		String refreshToken = meta.get("refreshToken");

		return ResponseEntity.ok()
				.header("X-Access-Token", accessToken)
				.header("X-Refresh-Token", refreshToken)
				.body(new AuthResponse(true, "User registered successfully", null)); 
	}


	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
		AuthResponse response = authService.login(loginRequest.getPhone());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/verify-login-otp")
	public ResponseEntity<AuthResponse> verifyLoginOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
		AuthResponse response = authService.verifyLoginOtp(verifyOtpRequest.getPhone(), verifyOtpRequest.getOtp());

		@SuppressWarnings("unchecked")
		Map<String, String> meta = (Map<String, String>) response.getData();
		String accessToken = meta.get("accessToken");
		String refreshToken = meta.get("refreshToken");

		return ResponseEntity.ok()
				.header("X-Access-Token", accessToken)
				.header("X-Refresh-Token", refreshToken)
				.body(new AuthResponse(true, "Login successful", null));
	}
}
