package com.zenmgt.config;

import com.zenmgt.dto.ApiResponse;
import com.zenmgt.enums.ErrorCodes;
import com.zenmgt.exception.BusinessException;
import com.zenmgt.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global Exception Handler for standardized API responses
 * Returns responses in format: {"code": "xxxx", "msg": "message", "data": null/data}
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ====== Business Logic Exceptions ======
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("Business exception: {} [URI: {}]", ex.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getData());
        return ResponseEntity.badRequest().body(response);
    }
    
    // ====== Validation Exceptions ======
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        log.warn("Validation exception: {} [URI: {}]", ex.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ex.getFieldErrors() != null 
            ? ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getFieldErrors())
            : ApiResponse.error(ex.getErrorCode(), ex.getMessage());
            
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Method argument validation failed [URI: {}]", request.getRequestURI());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        String message = "Input validation failed";
        if (!fieldErrors.isEmpty()) {
            message += ": " + String.join(", ", fieldErrors.values());
        }
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, message, fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        log.warn("Constraint violation [URI: {}]", request.getRequestURI());
        
        Map<String, String> fieldErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            fieldErrors.put(fieldName, message);
        }
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, 
            "Constraint validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    // ====== Authentication & Authorization Exceptions ======
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        log.warn("Authentication failed: {} [URI: {}]", ex.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.AUTH_INVALID_TOKEN, "Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        log.warn("Bad credentials [URI: {}]", request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.AUTH_USER_INVALID_PASSWORD, "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied: {} [URI: {}]", ex.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.AUTH_INSUFFICIENT_PERMISSIONS, "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    // ====== Data Access Exceptions ======
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        log.warn("Data integrity violation [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        // Try to determine specific constraint violation
        String message = ex.getMessage();
        ErrorCodes errorCode = ErrorCodes.DATABASE_CONSTRAINT_VIOLATION;
        
        if (message != null) {
            if (message.contains("username") || message.contains("UNIQUE")) {
                errorCode = ErrorCodes.USERNAME_ALREADY_EXISTS;
            } else if (message.contains("email")) {
                errorCode = ErrorCodes.EMAIL_ALREADY_EXISTS;
            } else if (message.contains("user_code")) {
                errorCode = ErrorCodes.USER_CODE_ALREADY_EXISTS;
            }
        }
        
        ApiResponse<Object> response = ApiResponse.error(errorCode);
        return ResponseEntity.badRequest().body(response);
    }
    
    // ====== HTTP Request Exceptions ======
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.warn("HTTP message not readable [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, "Invalid request format");
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("Method argument type mismatch [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Invalid parameter '%s': expected %s", 
            ex.getName(), ex.getRequiredType().getSimpleName());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, message);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        log.warn("Missing request parameter [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.FIELD_CANNOT_BE_EMPTY, message);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        log.warn("HTTP method not supported [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("HTTP method '%s' not supported for this endpoint", ex.getMethod());
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        log.warn("No handler found [URI: {}]", request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.ENTITY_NOT_FOUND, "Endpoint not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    // ====== Runtime Exceptions ======
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.warn("Illegal argument [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        
        log.warn("Illegal state [URI: {}]: {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.VALIDATION_FAILED, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    // ====== Generic Exception Handler ======
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error [URI: {}]: ", request.getRequestURI(), ex);
        
        // For security, don't expose internal error details
        ApiResponse<Object> response = ApiResponse.error(ErrorCodes.INTERNAL_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
