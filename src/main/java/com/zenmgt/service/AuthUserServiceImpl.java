package com.zenmgt.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.dto.MfaSetupDTO;
import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserCredential;
import com.zenmgt.mapper.AuthUserMapper;
import com.zenmgt.repository.AuthUserRepository;
import com.zenmgt.repository.AuthUserRepositoryCustom;
import com.zenmgt.repository.AuthUserCredentialRepository;
import com.zenmgt.repository.VersionControlRepository.VersionControlResult;
import com.zenmgt.util.SnowflakeIdGenerator;
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
    @Autowired private final SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired private final AuthUserMapper authUserMapper;
    @Autowired private final PasswordEncoder passwordEncoder;
    @Autowired private final JwtService jwtService;
    @Autowired private final SecretGenerator secretGenerator;
    @Autowired private final CodeVerifier codeVerifier;

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
        logger.debug("Attempting to authenticate user: {}", request.getUsername());
        
        Optional<AuthUser> userOpt = authUserRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            logger.debug("User not found: {}", request.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        AuthUser user = userOpt.get();
        logger.debug("Found user: {} with ID: {}", user.getUserCode(), user.getId());
        
        Optional<AuthUserCredential> credentialOpt = credentialRepository.findByParentId(user.getId());
        if (credentialOpt.isEmpty()) {
            logger.debug("No credentials found for user ID: {}", user.getId());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        AuthUserCredential credential = credentialOpt.get();
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), credential.getHashPassword());
        logger.debug("Password match result for user {}: {}", user.getUserCode(), passwordMatches);
        
        if (!passwordMatches) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }

        // Password is correct, now ENFORCE MFA registration
        if (!credential.getMfaEnabled()) {
            // MFA is not enabled - user MUST set up MFA before proceeding
            logger.debug("MFA not enabled for user: {} - requiring MFA setup", user.getUserCode());
            return ResponseEntity.ok(Map.of(
                "requireMfaSetup", true,
                "message", "MFA setup is required before you can login",
                "username", request.getUsername()
            ));
        }

        // MFA is enabled - require MFA code
        if (request.getMfaCode() == null) {
            logger.debug("MFA code required for user: {}", user.getUserCode());
            return ResponseEntity.ok(Map.of(
                "requireMfa", true,
                "message", "MFA verification required",
                "username", request.getUsername()
            ));
        }
        
        // Verify MFA code
        if (!verifyMfaCode(credential.getMfaSecret(), request.getMfaCode())) {
            logger.debug("Invalid MFA code for user: {}", user.getUserCode());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid MFA code"));
        }
        
        logger.debug("MFA verification successful for user: {}", user.getUserCode());

        // Only generate token if both password and MFA are verified
        String token = jwtService.generateToken(user);
        logger.debug("Successfully authenticated user: {}", user.getUserCode());
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
}
