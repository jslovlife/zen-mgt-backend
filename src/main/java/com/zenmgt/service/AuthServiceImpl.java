package com.zenmgt.service;

import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaSetupDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.model.AuthUser;
import com.zenmgt.model.AuthUserCredential;
import com.zenmgt.model.AuthUserDetail;
import com.zenmgt.repository.AuthUserCredentialRepository;
import com.zenmgt.repository.AuthUserDetailRepository;
import com.zenmgt.repository.UserRepository;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

import com.zenmgt.dto.ApiResponse;
import com.zenmgt.enums.ErrorCodes;
import com.zenmgt.service.TokenBlacklistService;

/**
 * Authentication Service Implementation
 * Handles authentication, MFA setup, and login operations
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final AuthUserCredentialRepository authUserCredentialRepository;
    private final AuthUserDetailRepository authUserDetailRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    // TOTP dependencies
    private final SecretGenerator secretGenerator;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;
    private final TokenBlacklistService tokenBlacklistService;
    
    // ====== Authentication Methods ======
    
    @Override
    @Transactional
    public ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest) {
        logger.debug("Authenticating user: {}", loginRequest.getUsername());
        
        try {
            // Step 1: Find user by username
            Optional<AuthUser> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                logger.warn("Authentication failed - user not found: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUser user = userOpt.get();
            
            // Step 2: Check if user is active
            if (user.getRecordStatus() != 1) { // 1 = ACTIVE
                logger.warn("Authentication failed - user not active: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_ACCOUNT_INACTIVE));
            }
            
            // Step 3: Get user credentials
            Optional<AuthUserCredential> credentialOpt = authUserCredentialRepository.findByParentId(user.getId());
            if (credentialOpt.isEmpty()) {
                logger.warn("Authentication failed - no credentials found for user: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUserCredential credential = credentialOpt.get();
            
            // Step 4: Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), credential.getHashPassword())) {
                logger.warn("Authentication failed - incorrect password for user: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_INVALID_PASSWORD));
            }
            
            // DEBUG: Log MFA settings for troubleshooting
            logger.info("DEBUG - User: {}, MFA Enabled: {}, MFA Enforced: {}, MFA Secret exists: {}", 
                loginRequest.getUsername(), 
                credential.getMfaEnabled(), 
                credential.getMfaEnforced(),
                credential.getMfaSecret() != null && !credential.getMfaSecret().isEmpty());
            
            // Step 5: Check if MFA is enabled
            if (credential.getMfaEnabled() != null && credential.getMfaEnabled()) {
                // MFA is enabled - require TOTP code
                if (!StringUtils.hasText(loginRequest.getMfaCode())) {
                    logger.debug("MFA required for user: {}", loginRequest.getUsername());
                    Map<String, Object> mfaData = Map.of(
                        "requiresMfa", true,
                        "status", "MFA_REQUIRED"
                    );
                    return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.error(ErrorCodes.AUTH_MFA_REQUIRED, "MFA verification required", mfaData));
                }
                
                // Verify MFA code
                if (!verifyMfaCode(credential.getMfaSecret(), loginRequest.getMfaCode())) {
                    logger.warn("Authentication failed - invalid MFA code for user: {}", loginRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(ErrorCodes.AUTH_MFA_INVALID_CODE));
                }
            }
            
            // Step 6: Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.updateById(user);
            
            // Step 7: Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Step 8: Get user details for response
            Optional<AuthUserDetail> detailOpt = authUserDetailRepository.findByParentId(user.getId());
            String email = detailOpt.map(AuthUserDetail::getEmail).orElse("");
            
            logger.info("Authentication successful for user: {}", loginRequest.getUsername());
            
            Map<String, Object> data = Map.of(
                "status", "SUCCESS",
                "message", "Authentication successful",
                "token", token,
                "user", Map.of(
                    "userCode", user.getUserCode(),
                    "username", loginRequest.getUsername(),
                    "email", email,
                    "mfaEnabled", credential.getMfaEnabled() != null && credential.getMfaEnabled()
                )
            );
            return ResponseEntity.ok(ApiResponse.success(data, "Authentication successful"));
            
        } catch (Exception e) {
            logger.error("Error authenticating user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "Authentication failed: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> verifyMfaCode(MfaVerificationDTO verificationDTO) {
        logger.debug("Verifying MFA code for user");
        
        try {
            // TODO: Implement MFA verification logic
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("error", "MFA verification not yet implemented"));
            
        } catch (Exception e) {
            logger.error("Error verifying MFA code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "MFA verification failed: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> handleOAuth2Login(String email, String name, String picture) {
        logger.debug("Handling OAuth2 login for email: {}", email);
        
        try {
            // TODO: Implement OAuth2 login logic
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("error", "OAuth2 login not yet implemented"));
            
        } catch (Exception e) {
            logger.error("Error handling OAuth2 login for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.EXTERNAL_SERVICE_ERROR, "OAuth2 login failed: " + e.getMessage()));
        }
    }
    
    // ====== MFA Methods ======
    
    @Override
    @Transactional
    public ResponseEntity<?> initiateMfaSetup(String username) {
        logger.debug("Initiating MFA setup for user: {}", username);
        
        try {
            // Find user
            Optional<AuthUser> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUser user = userOpt.get();
            
            // Get user credentials
            Optional<AuthUserCredential> credentialOpt = authUserCredentialRepository.findByParentId(user.getId());
            if (credentialOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUserCredential credential = credentialOpt.get();
            
            // Check if MFA is already enabled
            if (credential.getMfaEnabled() != null && credential.getMfaEnabled()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ErrorCodes.MFA_ALREADY_ENABLED, "MFA is already enabled for this user"));
            }
            
            // Generate new secret
            String secret = secretGenerator.generate();
            
            // Get user details for QR code
            Optional<AuthUserDetail> detailOpt = authUserDetailRepository.findByParentId(user.getId());
            String email = detailOpt.map(AuthUserDetail::getEmail).orElse(username + "@company.com");
            
            // Generate QR code data
            QrData qrData = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Zen Management System")
                .digits(6)
                .period(30)
                .build();
            
            // Generate QR code image (Base64)
            byte[] qrCodeBytes = qrGenerator.generate(qrData);
            String qrCodeImage = Base64.getEncoder().encodeToString(qrCodeBytes);
            
            // Store secret temporarily (not enabled yet)
            credential.setMfaSecret(secret);
            credential.setMfaEnabled(false); // Not enabled until verified
            authUserCredentialRepository.updateById(credential);
            
            logger.info("MFA setup initiated for user: {}", username);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "message", "MFA setup initiated",
                "secret", secret,
                "qrCode", qrCodeImage,
                "manualEntryKey", secret,
                "issuer", "Zen Management System",
                "accountName", email
            ), "MFA setup initiated"));
            
        } catch (Exception e) {
            logger.error("Error initiating MFA setup for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_SECRET_GENERATION_FAILED, "MFA setup failed: " + e.getMessage()));
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> verifyAndEnableMfa(MfaVerificationDTO mfaRequest) {
        logger.debug("Verifying and enabling MFA for user: {}", mfaRequest.getUsername());
        
        try {
            // Find user
            Optional<AuthUser> userOpt = userRepository.findByUsername(mfaRequest.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUser user = userOpt.get();
            
            // Get user credentials
            Optional<AuthUserCredential> credentialOpt = authUserCredentialRepository.findByParentId(user.getId());
            if (credentialOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUserCredential credential = credentialOpt.get();
            
            // Verify the MFA code
            if (!StringUtils.hasText(credential.getMfaSecret())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCodes.MFA_SETUP_REQUIRED, "MFA setup not initiated. Please start MFA setup first."));
            }
            
            if (!verifyMfaCode(credential.getMfaSecret(), mfaRequest.getMfaCode())) {
                logger.warn("MFA verification failed for user: {}", mfaRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_MFA_INVALID_CODE));
            }
            
            // Generate recovery codes
            List<String> recoveryCodes = generateRecoveryCodes();
            
            // Enable MFA
            credential.setMfaEnabled(true);
            credential.setRecoveryCodes(String.join(",", recoveryCodes));
            authUserCredentialRepository.updateById(credential);
            
            logger.info("MFA enabled successfully for user: {}", mfaRequest.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "message", "MFA enabled successfully",
                "mfaEnabled", true,
                "recoveryCodes", recoveryCodes
            ), "MFA enabled successfully"));
            
        } catch (Exception e) {
            logger.error("Error verifying MFA for user {}: {}", mfaRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "MFA verification failed: " + e.getMessage()));
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> disableMfa(String username, String mfaCode) {
        logger.debug("Disabling MFA for user: {}", username);
        
        try {
            // Find user
            Optional<AuthUser> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUser user = userOpt.get();
            
            // Get user credentials
            Optional<AuthUserCredential> credentialOpt = authUserCredentialRepository.findByParentId(user.getId());
            if (credentialOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUserCredential credential = credentialOpt.get();
            
            // Check if MFA is enabled
            if (credential.getMfaEnabled() == null || !credential.getMfaEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "MFA is not enabled for this user"));
            }
            
            // Verify MFA code before disabling
            if (!verifyMfaCode(credential.getMfaSecret(), mfaCode)) {
                logger.warn("MFA disable failed - invalid code for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "Invalid MFA code"));
            }
            
            // Disable MFA
            credential.setMfaEnabled(false);
            credential.setMfaSecret(null);
            credential.setRecoveryCodes(null);
            authUserCredentialRepository.updateById(credential);
            
            logger.info("MFA disabled successfully for user: {}", username);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "message", "MFA disabled successfully",
                "mfaEnabled", false
            ), "MFA disabled successfully"));
            
        } catch (Exception e) {
            logger.error("Error disabling MFA for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "MFA disable failed: " + e.getMessage()));
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> generateNewRecoveryCodes(String username, String mfaCode) {
        logger.debug("Generating new recovery codes for user: {}", username);
        
        try {
            // Find user and validate MFA
            Optional<AuthUser> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUser user = userOpt.get();
            Optional<AuthUserCredential> credentialOpt = authUserCredentialRepository.findByParentId(user.getId());
            if (credentialOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCodes.AUTH_USER_NOT_FOUND));
            }
            
            AuthUserCredential credential = credentialOpt.get();
            
            // Verify MFA is enabled and code is valid
            if (credential.getMfaEnabled() == null || !credential.getMfaEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCodes.MFA_NOT_ENABLED, "MFA is not enabled"));
            }
            
            if (!verifyMfaCode(credential.getMfaSecret(), mfaCode)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCodes.AUTH_MFA_INVALID_CODE));
            }
            
            // Generate new recovery codes
            List<String> newRecoveryCodes = generateRecoveryCodes();
            credential.setRecoveryCodes(String.join(",", newRecoveryCodes));
            authUserCredentialRepository.updateById(credential);
            
            logger.info("New recovery codes generated for user: {}", username);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "message", "New recovery codes generated",
                "recoveryCodes", newRecoveryCodes
            ), "New recovery codes generated"));
            
        } catch (Exception e) {
            logger.error("Error generating recovery codes for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_SECRET_GENERATION_FAILED, "Recovery code generation failed: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> setupMfa() {
        logger.debug("Setting up MFA for current user");
        
        try {
            // This method would typically get the current user from JWT token
            // For now, return a placeholder response
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error(ErrorCodes.MFA_SETUP_REQUIRED, "MFA setup requires authentication token. Use /mfa/setup/init with username instead."));
            
        } catch (Exception e) {
            logger.error("Error setting up MFA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.MFA_SECRET_GENERATION_FAILED, "MFA setup failed: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<?> logout(String token) {
        logger.debug("Logging out user with token");
        
        try {
            // Validate token format
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCodes.AUTH_INVALID_TOKEN, "Token is required for logout"));
            }
            
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Blacklist the token
            tokenBlacklistService.blacklistToken(token);
            
            logger.info("User logged out successfully");
            
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "SUCCESS",
                "message", "Logged out successfully"
            ), "Logged out successfully"));
            
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "Logout failed: " + e.getMessage()));
        }
    }
    
    // ====== Helper Methods ======
    
    /**
     * Verify TOTP code
     */
    private boolean verifyMfaCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            logger.error("Error verifying MFA code: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate recovery codes
     */
    private List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            // Generate 8-character alphanumeric codes
            String code = String.format("%08d", random.nextInt(100000000));
            codes.add(code);
        }
        
        return codes;
    }
} 