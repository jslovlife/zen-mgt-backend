package com.zenmgt.exception;

import com.zenmgt.enums.ErrorCodes;
import lombok.Getter;

import java.util.Map;

/**
 * Validation exception with field-specific error details
 */
@Getter
public class ValidationException extends RuntimeException {
    
    private final ErrorCodes errorCode;
    private final Map<String, String> fieldErrors;
    
    public ValidationException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }
    
    public ValidationException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }
    
    public ValidationException(ErrorCodes errorCode, Map<String, String> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(ErrorCodes errorCode, String customMessage, Map<String, String> fieldErrors) {
        super(customMessage);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }
} 