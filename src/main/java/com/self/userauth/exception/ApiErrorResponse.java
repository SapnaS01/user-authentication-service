package com.self.userauth.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiErrorResponse {
	private boolean success;
	private String message;
	private int statusCode;
	private LocalDateTime timestamp;
	private Map<String, String> errors; // For field-level validation errors (optional)

	public ApiErrorResponse(boolean success, String message, int statusCode) {
		this.success = success;
		this.message = message;
		this.statusCode = statusCode;
		this.timestamp = LocalDateTime.now();
	}

	public ApiErrorResponse(boolean success, String message, int statusCode, Map<String, String> errors) {
		this(success, message, statusCode);
		this.errors = errors;
	}
}
