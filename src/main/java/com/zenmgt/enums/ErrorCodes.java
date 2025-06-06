package com.zenmgt.enums;

/**
 * Standardized Error Codes and Messages
 * Format: ERROR_NAME("code", "message")
 */
public enum ErrorCodes {
    
    // Success
    SUCCESS("0000", "Success"),
    
    // Authentication & Authorization Errors (90xx)
    AUTH_USER_INVALID_USERNAME("9001", "Invalid Username"),
    AUTH_USER_INVALID_PASSWORD("9002", "Invalid Password"),
    AUTH_USER_NOT_FOUND("9003", "User Not Found"),
    AUTH_USER_ACCOUNT_LOCKED("9004", "User Account Locked"),
    AUTH_USER_ACCOUNT_INACTIVE("9005", "User Account Inactive"),
    AUTH_INVALID_TOKEN("9006", "Invalid Authentication Token"),
    AUTH_TOKEN_EXPIRED("9007", "Authentication Token Expired"),
    AUTH_INSUFFICIENT_PERMISSIONS("9008", "Insufficient Permissions"),
    AUTH_MFA_REQUIRED("9009", "Multi-Factor Authentication Required"),
    AUTH_MFA_INVALID_CODE("9010", "Invalid MFA Code"),
    
    // Validation Errors (91xx)
    VALIDATION_FAILED("9100", "Input Validation Failed"),
    FIELD_CANNOT_BE_EMPTY("9101", "Required Field Cannot Be Empty"),
    FIELD_TOO_LONG("9102", "Field Value Too Long"),
    FIELD_TOO_SHORT("9103", "Field Value Too Short"),
    INVALID_EMAIL_FORMAT("9104", "Invalid Email Format"),
    INVALID_DATE_FORMAT("9105", "Invalid Date Format"),
    INVALID_NUMBER_FORMAT("9106", "Invalid Number Format"),
    DUPLICATE_VALUE("9107", "Duplicate Value Not Allowed"),
    
    // Business Rule Errors (92xx)
    USER_ALREADY_EXISTS("9201", "User Already Exists"),
    EMAIL_ALREADY_EXISTS("9202", "Email Already Exists"),
    USERNAME_ALREADY_EXISTS("9203", "Username Already Exists"),
    USER_CODE_ALREADY_EXISTS("9204", "User Code Already Exists"),
    INVALID_USER_STATUS("9205", "Invalid User Status"),
    CANNOT_DELETE_ACTIVE_USER("9206", "Cannot Delete Active User"),
    USER_HAS_PENDING_APPROVALS("9207", "User Has Pending Approvals"),
    INVALID_RECORD_STATUS("9208", "Invalid Record Status"),
    
    // Data Access Errors (93xx)
    ENTITY_NOT_FOUND("9301", "Entity Not Found"),
    ENTITY_ALREADY_DELETED("9302", "Entity Already Deleted"),
    INVALID_ENTITY_ID("9303", "Invalid Entity ID"),
    ENCRYPTED_ID_INVALID("9304", "Invalid Encrypted ID"),
    DATABASE_CONSTRAINT_VIOLATION("9305", "Database Constraint Violation"),
    CONCURRENT_MODIFICATION("9306", "Entity Modified By Another User"),
    
    // Performance Errors (94xx)
    QUERY_TIMEOUT("9401", "Query Execution Timeout"),
    DATASET_TOO_LARGE("9402", "Dataset Too Large - Use Pagination"),
    RATE_LIMIT_EXCEEDED("9403", "Rate Limit Exceeded"),
    
    // Approval System Errors (95xx)
    APPROVAL_REQUEST_NOT_FOUND("9501", "Approval Request Not Found"),
    APPROVAL_ALREADY_PROCESSED("9502", "Approval Request Already Processed"),
    INVALID_APPROVAL_STATUS("9503", "Invalid Approval Status"),
    INSUFFICIENT_APPROVAL_PERMISSIONS("9504", "Insufficient Approval Permissions"),
    
    // MFA Errors (96xx)
    MFA_NOT_ENABLED("9601", "MFA Not Enabled"),
    MFA_ALREADY_ENABLED("9602", "MFA Already Enabled"),
    MFA_SETUP_REQUIRED("9603", "MFA Setup Required"),
    INVALID_RECOVERY_CODE("9604", "Invalid Recovery Code"),
    MFA_SECRET_GENERATION_FAILED("9605", "MFA Secret Generation Failed"),
    
    // System Errors (99xx)
    INTERNAL_ERROR("9999", "System Internal Error"),
    SERVICE_UNAVAILABLE("9998", "Service Temporarily Unavailable"),
    CONFIGURATION_ERROR("9997", "System Configuration Error"),
    EXTERNAL_SERVICE_ERROR("9996", "External Service Error");
    
    private final String code;
    private final String message;
    
    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * Get formatted message with placeholders replaced
     */
    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s", code, message);
    }
} 