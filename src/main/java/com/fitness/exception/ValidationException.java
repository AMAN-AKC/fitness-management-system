package com.fitness.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String fieldName, String detail) {
		super(String.format("Please provide a valid %s. %s", fieldName, detail));
	}
}
