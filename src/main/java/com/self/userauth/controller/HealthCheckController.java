package com.self.userauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> healthCheck() {
		Map<String, Object> status = new HashMap<>();
		status.put("status", "UP");
		status.put("timestamp", Instant.now());
		status.put("service", "UserAuthService");
		return ResponseEntity.ok(status);
	}
}
