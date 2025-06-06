package com.zenmgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zenmgt.enums.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API Response Format
 * {
 *   "code": "0000 for success, error code for errors",
 *   "msg": "message",
 *   "data": "actual response data"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("msg")
    private String msg;
    
    @JsonProperty("data")
    private T data;
    
    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(ErrorCodes.SUCCESS.getCode())
            .msg(ErrorCodes.SUCCESS.getMessage())
            .data(data)
            .build();
    }
    
    /**
     * Create successful response with custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .code(ErrorCodes.SUCCESS.getCode())
            .msg(message)
            .data(data)
            .build();
    }
    
    /**
     * Create error response with ErrorCodes enum
     */
    public static <T> ApiResponse<T> error(ErrorCodes errorCode) {
        return ApiResponse.<T>builder()
            .code(errorCode.getCode())
            .msg(errorCode.getMessage())
            .data(null)
            .build();
    }
    
    /**
     * Create error response with ErrorCodes enum and custom message
     */
    public static <T> ApiResponse<T> error(ErrorCodes errorCode, String customMessage) {
        return ApiResponse.<T>builder()
            .code(errorCode.getCode())
            .msg(customMessage)
            .data(null)
            .build();
    }
    
    /**
     * Create error response with ErrorCodes enum, custom message, and data
     */
    public static <T> ApiResponse<T> error(ErrorCodes errorCode, String customMessage, T errorData) {
        return ApiResponse.<T>builder()
            .code(errorCode.getCode())
            .msg(customMessage)
            .data(errorData)
            .build();
    }
    
    /**
     * Create error response with custom code and message
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .msg(message)
            .data(null)
            .build();
    }
    
    /**
     * Create error response with data (for validation errors with field details)
     */
    public static <T> ApiResponse<T> error(ErrorCodes errorCode, T errorData) {
        return ApiResponse.<T>builder()
            .code(errorCode.getCode())
            .msg(errorCode.getMessage())
            .data(errorData)
            .build();
    }
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return ErrorCodes.SUCCESS.getCode().equals(code);
    }
    
    /**
     * Check if response is error
     */
    public boolean isError() {
        return !isSuccess();
    }
} 