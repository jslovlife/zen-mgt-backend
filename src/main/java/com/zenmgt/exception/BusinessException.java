package com.zenmgt.exception;

import com.zenmgt.enums.ErrorCodes;
import lombok.Getter;

/**
 * Business logic exception with standardized error codes
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCodes errorCode;
    private final Object data;
    
    public BusinessException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public BusinessException(ErrorCodes errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public BusinessException(ErrorCodes errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }
    
    public BusinessException(ErrorCodes errorCode, String customMessage, Object data) {
        super(customMessage);
        this.errorCode = errorCode;
        this.data = data;
    }
    
    public BusinessException(ErrorCodes errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.data = null;
    }
} 