package com.zenmgt.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.task.TaskExecutor;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.dto.MfaSetupDTO;
import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserCredential;
import com.zenmgt.entity.AuthUserDetail;
import com.zenmgt.entity.mapper.AuthUserMapper;
import com.zenmgt.repository.AuthUserRepository;
import com.zenmgt.repository.AuthUserRepositoryCustom;
import com.zenmgt.repository.AuthUserCredentialRepository;
import com.zenmgt.repository.AuthUserDetailRepository;
import com.zenmgt.repository.VersionControlRepository.VersionControlResult;
import com.zenmgt.util.SnowflakeIdGenerator;
import com.zenmgt.util.TraceIdUtil;
import com.zenmgt.enums.RecordStatus;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.SecretGenerator;
import java.util.ArrayList;
import java.util.Base64;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {
    private static final Logger logger = LoggerFactory.getLogger(AuthUserServiceImpl.class);

    @Autowired private final AuthUserRepository authUserRepository;
    @Autowired private final AuthUserRepositoryCustom authUserRepositoryCustom;
    @Autowired private final AuthUserCredentialRepository credentialRepository;
    @Autowired private final AuthUserDetailRepository authUserDetailRepository;
    @Autowired private final SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired private final AuthUserMapper authUserMapper;
    @Autowired private final PasswordEncoder passwordEncoder;
    @Autowired private final JwtService jwtService;
    @Autowired private final SecretGenerator secretGenerator;
    @Autowired private final CodeVerifier codeVerifier;
    
    @Autowired
    @Qualifier("mfaTaskExecutor")
    private TaskExecutor mfaTaskExecutor;
    
    @Autowired
    @Qualifier("securityTaskExecutor")
    private TaskExecutor securityTaskExecutor;

    @Override
    public List<AuthUserDTO> getAllUsers() {
        return authUserRepository.findAll().stream()
                .map(authUserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AuthUserDTO> getUserById(Long id) {
        return authUserRepository.findById(id)
                .map(authUserMapper::toDto);
    }

    @Override
    public Optional<AuthUserDTO> getUserByCode(String userCode) {
        return authUserRepository.findByUserCode(userCode)
                .map(authUserMapper::toDto);
    }

    @Override
    @Transactional
    public AuthUserDTO createUser(AuthUserDTO userDTO) {
        Long userId = snowflakeIdGenerator.nextId();
        Long detailId = snowflakeIdGenerator.nextId();
        Long approvalId = snowflakeIdGenerator.nextId();
        
        // Set default values
        userDTO.setUserCode(String.format("USER%d", userId));
 
        // Create user with approval request
        VersionControlResult<AuthUser> result = authUserRepositoryCustom.createWithDetailAndApproval(
            userDTO, userId, detailId, approvalId, getCurrentUserId()
        );
             
        // Return DTO with approval status
        AuthUserDTO response = authUserMapper.toDto(result.getEntity());
        response.setStatus(result.getApprovalStatus());
        return response;
    }

    @Override
    @Transactional
    public AuthUserDTO updateUser(Long id, AuthUserDTO userDTO) {
        Optional<AuthUser> existingUserOpt = authUserRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return null;
        }

        Long newDetailId = snowflakeIdGenerator.nextId();
        Long approvalId = snowflakeIdGenerator.nextId();
        
        // Create new version with approval request
        VersionControlResult<AuthUser> result = authUserRepositoryCustom.updateWithNewDetailAndApproval(
            id, userDTO, newDetailId, approvalId, getCurrentUserId()
        );

        // Return DTO with approval status
        AuthUserDTO response = authUserMapper.toDto(result.getEntity());
        response.setStatus(result.getApprovalStatus());
        return response;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        Optional<AuthUser> userOpt = authUserRepository.findById(id);
        if (userOpt.isEmpty()) {
            return false;
        }

        AuthUser user = userOpt.get();
        user.setIsActive(RecordStatus.DELETED.getValue());
            authUserRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public ResponseEntity<?> authenticateUser(LoginRequestDTO request) {
        String traceId = TraceIdUtil.getTraceId();
        logger.debug("Attempting to authenticate user: {} [TraceId: {}]", request.getUsername(), traceId);
        
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            logger.debug("User not found: {} [TraceId: {}]", request.getUsername(), traceId);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        AuthUser user = userOpt.get();
        // Set user ID in MDC for subsequent logging
        TraceIdUtil.setUserId(user.getUserCode());
        logger.debug("Found user: {} with ID: {} [TraceId: {}]", user.getUserCode(), user.getId(), traceId);
        
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            logger.debug("No credentials found for user ID: {} [TraceId: {}]", user.getId(), traceId);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        AuthUserCredential credential = credentialOpt.get();
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), credential.getHashPassword());
        logger.debug("Password match result for user {}: {} [TraceId: {}]", user.getUserCode(), passwordMatches, traceId);
        
        if (!passwordMatches) {
            logger.warn("Authentication failed - invalid password for user: {} [TraceId: {}]", user.getUserCode(), traceId);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        // Password is correct, now ENFORCE MFA registration
        if (!credential.getMfaEnabled()) {
            // MFA is not enabled - user MUST set up MFA before proceeding
            logger.debug("MFA not enabled for user: {} - requiring MFA setup [TraceId: {}]", user.getUserCode(), traceId);
            return ResponseEntity.ok(Map.of(
                "requireMfaSetup", true,
                "message", "MFA setup is required before you can login",
                "username", request.getUsername()
            ));
        }

        // MFA is enabled - require MFA code
        if (request.getMfaCode() == null) {
            logger.debug("MFA code required for user: {} [TraceId: {}]", user.getUserCode(), traceId);
            return ResponseEntity.ok(Map.of(
                "requireMfa", true,
                "message", "MFA verification required",
                "username", request.getUsername()
            ));
        }
        
        // Verify MFA code
        if (!verifyMfaCode(credential.getMfaSecret(), request.getMfaCode())) {
            logger.warn("Authentication failed - invalid MFA code for user: {} [TraceId: {}]", user.getUserCode(), traceId);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid MFA code"));
        }
        
        logger.debug("MFA verification successful for user: {} [TraceId: {}]", user.getUserCode(), traceId);

        // Only generate token if both password and MFA are verified
        String token = jwtService.generateToken(user);
        logger.info("Successfully authenticated user: {} [TraceId: {}]", user.getUserCode(), traceId);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "message", "Login successful"
        ));
    }

    @Override
    @Transactional
    public ResponseEntity<?> handleOAuth2Login(String email, String name, String picture) {
        AuthUser user = authUserRepository.findByEmail(email)
            .orElseGet(() -> createUserFromOAuth2(email, name, picture));

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", email,
            "name", name,
            "picture", picture
        ));
    }

    @Override
    @Transactional
    public ResponseEntity<?> setupMfa() {
        String secret = secretGenerator.generate();
        return ResponseEntity.ok(Map.of(
            "secret", secret,
            "qrCodeUrl", generateQrCodeUrl(secret)
        ));
    }

    @Override
    public ResponseEntity<?> verifyMfaCode(MfaVerificationDTO dto) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(dto.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        AuthUser user = userOpt.get();
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty() || !credentialOpt.get().getMfaEnabled()) {
            return ResponseEntity.badRequest().body(Map.of("error", "MFA not enabled"));
        }

        if (!verifyMfaCode(credentialOpt.get().getMfaSecret(), dto.getMfaCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid MFA code"));
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Override
    public ResponseEntity<?> initiateMfaSetup(String username) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        AuthUser user = userOpt.get();
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User credentials not found"));
        }

        AuthUserCredential credential = credentialOpt.get();
        if (credential.getMfaEnabled()) {
            return ResponseEntity.badRequest().body(Map.of("error", "MFA is already enabled"));
        }

        // Generate new secret
        String secret = secretGenerator.generate();
        
        // Generate QR code URL
        String qrCodeUrl = String.format("otpauth://totp/ZenMgt:%s?secret=%s&issuer=ZenMgt", 
            username, secret);

        // Generate recovery codes (we'll save these after verification)
        List<String> recoveryCodes = generateRecoveryCodes();

        // Store the secret temporarily (it will be saved permanently after verification)
        credential.setMfaSecret(secret);
        credentialRepository.save(credential);

        return ResponseEntity.ok(Map.of(
            "secret", secret,
            "qrCodeUrl", qrCodeUrl,
            "recoveryCodes", recoveryCodes
        ));
    }

    @Override
    public ResponseEntity<?> verifyAndEnableMfa(MfaSetupDTO setupDTO) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(setupDTO.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        AuthUser user = userOpt.get();
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User credentials not found"));
        }

        AuthUserCredential credential = credentialOpt.get();
        
        // Verify password first
        if (!passwordEncoder.matches(setupDTO.getPassword(), credential.getHashPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
        }

        // Verify MFA code
        if (!verifyMfaCode(credential.getMfaSecret(), setupDTO.getMfaCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid MFA code"));
        }

        // Enable MFA and save recovery codes
        credential.setMfaEnabled(true);
        List<String> recoveryCodes = generateRecoveryCodes();
        credential.setRecoveryCodes(String.join(",", recoveryCodes));
        credentialRepository.save(credential);

        // Generate JWT token for immediate login after MFA setup
        String token = jwtService.generateToken(user);
        
        return ResponseEntity.ok(Map.of(
            "message", "MFA enabled successfully",
            "token", token,
            "recoveryCodes", recoveryCodes
        ));
    }
    
    @Override
    public ResponseEntity<?> disableMfa(MfaSetupDTO setupDTO) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(setupDTO.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        AuthUser user = userOpt.get();
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User credentials not found"));
        }

        AuthUserCredential credential = credentialOpt.get();

        // Verify password
        if (!passwordEncoder.matches(setupDTO.getPassword(), credential.getHashPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
        }

        // Verify MFA code or recovery code
        boolean isValidMfa = setupDTO.getMfaCode() != null && 
            verifyMfaCode(credential.getMfaSecret(), setupDTO.getMfaCode());
        boolean isValidRecovery = setupDTO.getRecoveryCode() != null && 
            credential.getRecoveryCodes().contains(setupDTO.getRecoveryCode());

        if (!isValidMfa && !isValidRecovery) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
        }

        // Disable MFA
        credential.setMfaEnabled(false);
        credential.setMfaSecret(null);
        credential.setRecoveryCodes(null);
        credentialRepository.save(credential);

        return ResponseEntity.ok(Map.of("message", "MFA disabled successfully"));
    }
    
    @Override
    public ResponseEntity<?> generateNewRecoveryCodes(MfaSetupDTO setupDTO) {
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(setupDTO.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        AuthUser user = userOpt.get();
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User credentials not found"));
        }

        AuthUserCredential credential = credentialOpt.get();

        // Verify password
        if (!passwordEncoder.matches(setupDTO.getPassword(), credential.getHashPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
        }

        // Verify MFA code
        if (!verifyMfaCode(credential.getMfaSecret(), setupDTO.getMfaCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid MFA code"));
        }

        // Generate and save new recovery codes
        List<String> newRecoveryCodes = generateRecoveryCodes();
        credential.setRecoveryCodes(String.join(",", newRecoveryCodes));
        credentialRepository.save(credential);

        return ResponseEntity.ok(Map.of(
            "recoveryCodes", newRecoveryCodes,
            "message", "New recovery codes generated successfully"
        ));
    }

    private boolean verifyMfaCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    private String generateQrCodeUrl(String secret) {
        return "otpauth://totp/ZenMgt:" + secret;
    }

    private AuthUser createUserFromOAuth2(String email, String name, String picture) {
        // TODO: Implement user creation from OAuth2 data
        return null;
    }

    // TODO: Implement this method to get current user ID from security context
    private Long getCurrentUserId() {
        return 1L; // Temporary implementation
    }

    private List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            byte[] bytes = new byte[5];
            random.nextBytes(bytes);
            codes.add(Base64.getEncoder().encodeToString(bytes).substring(0, 8));
        }
        return codes;
    }

    /**
     * Async method for MFA code verification to improve performance
     */
    @Async("mfaTaskExecutor")
    public CompletableFuture<Boolean> verifyMfaCodeAsync(String secret, String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Async MFA verification started for thread: {}", Thread.currentThread().getName());
                boolean result = codeVerifier.isValidCode(secret, code);
                logger.debug("Async MFA verification completed for thread: {}", Thread.currentThread().getName());
                return result;
            } catch (Exception e) {
                logger.error("Error in async MFA verification: {}", e.getMessage());
                return false;
            }
        }, mfaTaskExecutor);
    }

    /**
     * Async method for generating recovery codes
     */
    @Async("mfaTaskExecutor")
    public CompletableFuture<List<String>> generateRecoveryCodesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Async recovery codes generation started for thread: {}", Thread.currentThread().getName());
            List<String> codes = generateRecoveryCodes();
            logger.debug("Async recovery codes generation completed for thread: {}", Thread.currentThread().getName());
            return codes;
        }, mfaTaskExecutor);
    }

    /**
     * Async method for JWT token generation
     */
    @Async("securityTaskExecutor")
    public CompletableFuture<String> generateTokenAsync(AuthUser user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Async JWT generation started for user: {} on thread: {}", 
                    user.getUserCode(), Thread.currentThread().getName());
                String token = jwtService.generateToken(user);
                logger.debug("Async JWT generation completed for user: {} on thread: {}", 
                    user.getUserCode(), Thread.currentThread().getName());
                return token;
            } catch (Exception e) {
                logger.error("Error in async JWT generation for user {}: {}", user.getUserCode(), e.getMessage());
                throw new RuntimeException("Failed to generate JWT token", e);
            }
        }, securityTaskExecutor);
    }

    /**
     * Thread-safe method to log authentication attempts
     */
    @Async("securityTaskExecutor")
    public CompletableFuture<Void> logAuthenticationAttemptAsync(String username, boolean success, String clientInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Authentication attempt - User: {}, Success: {}, Client: {}, Thread: {}", 
                    username, success, clientInfo, Thread.currentThread().getName());
                // Here you could add database logging or audit trail
            } catch (Exception e) {
                logger.error("Error logging authentication attempt: {}", e.getMessage());
            }
        }, securityTaskExecutor);
    }

    @Override
    @Transactional
    public boolean updateSessionValidity(Long userId, Long sessionValidityMs) {
        try {
            Optional<AuthUser> userOpt = authUserRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found with ID: {}", userId);
                return false;
            }

            AuthUser user = userOpt.get();
            Optional<AuthUserDetail> userDetailOpt = authUserDetailRepository.findByParentId(user.getId());
            
            if (userDetailOpt.isEmpty()) {
                logger.warn("User detail not found for user ID: {}", userId);
                return false;
            }

            AuthUserDetail userDetail = userDetailOpt.get();
            userDetail.setSessionValidity(sessionValidityMs);
            authUserDetailRepository.save(userDetail);

            logger.info("Session validity updated for user {} to {} ms ({} hours)", 
                user.getUserCode(), sessionValidityMs, sessionValidityMs / (60 * 60 * 1000.0));
            
            return true;
        } catch (Exception e) {
            logger.error("Error updating session validity for user ID {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
