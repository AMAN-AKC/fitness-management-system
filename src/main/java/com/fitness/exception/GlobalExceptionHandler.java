package com.fitness.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// ── Standard error response body ────────────────────────────────────────
	private ResponseEntity<Map<String, Object>> buildError(
			HttpStatus status, String error, String message, String path) {

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", status.value());
		body.put("error", error);
		body.put("message", message);
		body.put("path", path);
		return ResponseEntity.status(status).body(body);
	}

	// ── 404 Not Found ───────────────────────────────────────────────────────
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFound(
			ResourceNotFoundException ex, WebRequest request) {
		log.warn("ResourceNotFound: {}", ex.getMessage());
		return buildError(HttpStatus.NOT_FOUND, "Not Found",
				ex.getMessage(), request.getDescription(false));
	}

	// ── 409 Conflict ────────────────────────────────────────────────────────
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicateResource(
			DuplicateResourceException ex, WebRequest request) {
		log.warn("DuplicateResource: {}", ex.getMessage());
		return buildError(HttpStatus.CONFLICT, "Conflict",
				ex.getMessage(), request.getDescription(false));
	}

	// ── 403 Forbidden ───────────────────────────────────────────────────────
	@ExceptionHandler({ UnauthorizedAccessException.class, AccessDeniedException.class })
	public ResponseEntity<Map<String, Object>> handleUnauthorized(
			RuntimeException ex, WebRequest request) {
		log.warn("AccessDenied: {}", ex.getMessage());
		return buildError(HttpStatus.FORBIDDEN, "Forbidden",
				ex.getMessage(), request.getDescription(false));
	}

	// ── 400 Bad Request — Bean Validation (@Valid) ──────────────────────────
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(
			MethodArgumentNotValidException ex, WebRequest request) {

		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String field = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			fieldErrors.put(field, message);
		});

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Validation Failed");
		body.put("message", "One or more fields have invalid values.");
		body.put("fieldErrors", fieldErrors);
		body.put("path", request.getDescription(false));

		log.warn("ValidationFailed: {}", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// ── 400 Bad Request — custom ValidationException ────────────────────────
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(
			ValidationException ex, WebRequest request) {
		log.warn("Validation: {}", ex.getMessage());
		return buildError(HttpStatus.BAD_REQUEST, "Validation Failed",
				ex.getMessage(), request.getDescription(false));
	}

	// ── 422 Unprocessable — business rule violation ─────────────────────────
	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<Map<String, Object>> handleBusinessRule(
			BusinessRuleException ex, WebRequest request) {
		log.warn("BusinessRule: {}", ex.getMessage());
		return buildError(HttpStatus.UNPROCESSABLE_ENTITY, "Business Rule Violation",
				ex.getMessage(), request.getDescription(false));
	}

	// ── 401 Unauthorized — bad credentials ──────────────────────────────────
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleBadCredentials(
			BadCredentialsException ex, WebRequest request) {
		log.warn("BadCredentials from: {}", request.getDescription(false));
		return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized",
				"Invalid username or password.", request.getDescription(false));
	}

	// ── 401 Unauthorized — account disabled ─────────────────────────────────
	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<Map<String, Object>> handleDisabled(
			DisabledException ex, WebRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized",
				"Account is deactivated. Contact admin.", request.getDescription(false));
	}

	// ── 401 Unauthorized — account locked ───────────────────────────────────
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<Map<String, Object>> handleLocked(
			LockedException ex, WebRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized",
				"Account is locked due to too many failed attempts.",
				request.getDescription(false));
	}

	// ── 500 Internal Server Error — catch-all ───────────────────────────────
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneral(
			Exception ex, WebRequest request) {
		log.error("Unhandled exception at [{}]: ", request.getDescription(false), ex);
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
				"An unexpected error occurred. Please try again later.",
				request.getDescription(false));
	}
}