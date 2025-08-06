package com.self.userauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.self.userauth.pojo.AuthResponse;
import com.self.userauth.pojo.SignUpRequest;
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
}
