package com.zenmgt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * MFA Setup DTO
 * Used for MFA setup responses containing QR code and secret information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaSetupDTO {
    
    private String status;
    private String message;
    private String secret;
    private String qrCode; // Base64 encoded QR code image
    private String manualEntryKey;
    private String issuer;
    private String accountName;
    private boolean mfaEnabled;
    private List<String> recoveryCodes;
} 