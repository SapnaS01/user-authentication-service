package com.self.userauth.exception;

public class BadRequestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new BadRequestException with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public BadRequestException(String message) {
		super(message);
	}
}
