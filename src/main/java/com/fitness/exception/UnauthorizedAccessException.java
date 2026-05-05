package com.fitness.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

	public UnauthorizedAccessException(String message) {
		super(message);
	}

	public UnauthorizedAccessException() {
		super("You do not have permission to perform this action.");
	}
}