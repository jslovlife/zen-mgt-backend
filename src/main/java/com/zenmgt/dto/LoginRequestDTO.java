package com.zenmgt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login Request DTO
 * Used for username/password authentication with optional MFA code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
    
    // Optional MFA code - provided when MFA is enabled
    @Size(min = 6, max = 6, message = "MFA code must be 6 digits")
    private String mfaCode;
    
    // Flag to indicate if this is a recovery code instead of TOTP code
    private boolean isRecoveryCode = false;
} 