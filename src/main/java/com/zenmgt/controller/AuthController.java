package com.zenmgt.controller;

import com.zenmgt.dto.ApiResponse;
import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.enums.ErrorCodes;
import com.zenmgt.exception.BusinessException;
import com.zenmgt.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles login and authentication operations
 */
@RestController
@RequestMapping(value = "/mgt/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Value("${spring.security.oauth2.enabled:false}")
    private boolean oauth2Enabled;

    @Value("${app.auth.password-auth-enabled:true}")
    private boolean passwordAuthEnabled;

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getAuthConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("oauth2Enabled", oauth2Enabled);
        config.put("passwordAuthEnabled", passwordAuthEnabled);
        if (oauth2Enabled) {
            config.put("googleAuthUrl", "/oauth2/authorization/google");
        }
        return ApiResponse.success(config, "Authentication configuration retrieved");
    }

    @GetMapping("/oauth2/login")
    public ApiResponse<Map<String, String>> oauthLogin() {
        if (!oauth2Enabled) {
            throw new BusinessException(ErrorCodes.CONFIGURATION_ERROR, "OAuth2 authentication is disabled");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("googleAuthUrl", "/oauth2/authorization/google");
        return ApiResponse.success(response, "OAuth2 login URL provided");
    }

    /**
     * User Login
     * Supports both regular login and MFA-enabled login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            ResponseEntity<?> response = authService.authenticateUser(loginRequest);
            
            // Log the result without exposing sensitive data
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Login successful for user: {}", loginRequest.getUsername());
            } else {
                logger.warn("Login failed for user: {} with status: {}", 
                    loginRequest.getUsername(), response.getStatusCode());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Login error for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/mfa/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerificationDTO verificationDTO) {
        try {
            return authService.verifyMfaCode(verificationDTO);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.AUTH_MFA_INVALID_CODE, e.getMessage());
        }
    }

    @GetMapping("/oauth2/login/success")
    public ResponseEntity<?> loginSuccess(OAuth2AuthenticationToken authentication) {
        if (!oauth2Enabled) {
            throw new BusinessException(ErrorCodes.CONFIGURATION_ERROR, "OAuth2 authentication is disabled");
        }
        
        try {
            OAuth2User user = authentication.getPrincipal();
            String email = user.getAttribute("email");
            String name = user.getAttribute("name");
            String picture = user.getAttribute("picture");
            return authService.handleOAuth2Login(email, name, picture);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_TOKEN, e.getMessage());
        }
    }

    @GetMapping("/oauth2/login/failure")
    public ApiResponse<Object> loginFailure() {
        throw new BusinessException(ErrorCodes.AUTH_INVALID_TOKEN, "OAuth2 login failed");
    }

    @PostMapping(value = "/mfa/setup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> setupMfa() {
        if (!passwordAuthEnabled) {
            throw new BusinessException(ErrorCodes.CONFIGURATION_ERROR, "MFA setup is only available for password authentication");
        }
        
        try {
            return authService.setupMfa();
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.MFA_SECRET_GENERATION_FAILED, e.getMessage());
        }
    }

    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(java.util.Map.of(
            "status", "UP",
            "service", "Authentication Service",
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
    
    /**
     * Get authentication status for current user
     * This would typically be used with JWT token validation
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        // This endpoint would typically validate JWT token and return user info
        // For now, just return a placeholder response
        return ResponseEntity.ok().body(java.util.Map.of(
            "message", "Authentication status endpoint",
            "note", "JWT validation would be implemented here"
        ));
    }

    /**
     * User Logout
     * Invalidates JWT token by adding it to blacklist
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        logger.info("Logout request received");
        
        try {
            // Extract token from Authorization header
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Logout failed: No valid token provided");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCodes.AUTH_INVALID_TOKEN, "No valid token provided for logout"));
            }
            
            return authService.logout(token);
            
        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "Logout failed: " + e.getMessage()));
        }
    }
} 