package com.zenmgt.service;

import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import org.springframework.http.ResponseEntity;

/**
 * Authentication Service Interface
 * Handles authentication, MFA setup, and login operations
 */
public interface AuthService {
    
    // ====== Authentication Methods ======
    
    /**
     * Authenticate user with username/password
     * @param loginRequest Login credentials
     * @return Authentication response
     */
    ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest);
    
    /**
     * Verify MFA code during login
     * @param verificationDTO MFA verification data
     * @return Verification response
     */
    ResponseEntity<?> verifyMfaCode(MfaVerificationDTO verificationDTO);
    
    /**
     * Handle OAuth2 login
     * @param email User email from OAuth2 provider
     * @param name User name from OAuth2 provider
     * @param picture User profile picture from OAuth2 provider
     * @return OAuth2 login response
     */
    ResponseEntity<?> handleOAuth2Login(String email, String name, String picture);
    
    // ====== MFA Operations ======
    
    /**
     * Setup MFA for current authenticated user
     * @return MFA setup response
     */
    ResponseEntity<?> setupMfa();
    
    /**
     * Initiate MFA setup for a specific user
     * @param username Username to setup MFA for
     * @return MFA initiation response with QR code and secret
     */
    ResponseEntity<?> initiateMfaSetup(String username);
    
    /**
     * Verify MFA code and enable MFA for user
     * @param verificationDTO MFA verification data with username and code
     * @return MFA enable response with recovery codes
     */
    ResponseEntity<?> verifyAndEnableMfa(MfaVerificationDTO verificationDTO);
    
    /**
     * Disable MFA for user
     * @param username Username to disable MFA for
     * @param mfaCode MFA verification code
     * @return MFA disable response
     */
    ResponseEntity<?> disableMfa(String username, String mfaCode);
    
    /**
     * Generate new recovery codes for user
     * @param username Username to generate codes for
     * @param mfaCode MFA verification code
     * @return New recovery codes response
     */
    ResponseEntity<?> generateNewRecoveryCodes(String username, String mfaCode);

    /**
     * Logout user and invalidate token
     */
    ResponseEntity<?> logout(String token);
} 