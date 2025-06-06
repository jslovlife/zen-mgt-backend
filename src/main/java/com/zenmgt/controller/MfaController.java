package com.zenmgt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zenmgt.dto.MfaVerificationDTO;
import lombok.RequiredArgsConstructor;
import com.zenmgt.service.AuthService;

import jakarta.validation.Valid;

/**
 * MFA Controller
 * Handles Multi-Factor Authentication operations
 */
@RestController
@RequestMapping("/mgt/v1/mfa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MfaController {
    
    private final AuthService authService;

    /**
     * Initiate MFA setup for a user
     * Returns QR code and secret for TOTP app setup
     */
    @GetMapping("/setup/init")
    public ResponseEntity<?> initiateMfaSetup(@RequestParam String username) {
        return authService.initiateMfaSetup(username);
    }

    /**
     * Verify MFA code and enable MFA for the user
     * Returns recovery codes upon successful verification
     */
    @PostMapping("/setup/verify")
    public ResponseEntity<?> verifyAndEnableMfa(@Valid @RequestBody MfaVerificationDTO verificationDTO) {
        return authService.verifyAndEnableMfa(verificationDTO);
    }

    /**
     * Verify MFA code during login
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(@Valid @RequestBody MfaVerificationDTO verificationDTO) {
        return authService.verifyMfaCode(verificationDTO);
    }

    /**
     * Disable MFA for a user
     * Requires valid MFA code for verification
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disableMfa(@Valid @RequestBody MfaVerificationDTO verificationDTO) {
        return authService.disableMfa(verificationDTO.getUsername(), verificationDTO.getMfaCode());
    }

    /**
     * Generate new recovery codes
     * Requires valid MFA code for verification
     */
    @PostMapping("/recovery-codes/generate")
    public ResponseEntity<?> generateNewRecoveryCodes(@Valid @RequestBody MfaVerificationDTO verificationDTO) {
        return authService.generateNewRecoveryCodes(verificationDTO.getUsername(), verificationDTO.getMfaCode());
    }

    /**
     * Health check endpoint for MFA service
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(java.util.Map.of(
            "status", "UP",
            "service", "MFA Service",
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
} 