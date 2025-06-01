package com.zenmgt.service;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.dto.MfaSetupDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface AuthUserService {
    List<AuthUserDTO> getAllUsers();
    Optional<AuthUserDTO> getUserById(Long id);
    Optional<AuthUserDTO> getUserByCode(String userCode);
    AuthUserDTO createUser(AuthUserDTO userDTO);
    AuthUserDTO updateUser(Long id, AuthUserDTO userDTO);
    boolean deleteUser(Long id);
    
    // Authentication methods
    ResponseEntity<?> authenticateUser(LoginRequestDTO request);
    ResponseEntity<?> handleOAuth2Login(String email, String name, String picture);
    ResponseEntity<?> setupMfa();
    ResponseEntity<?> verifyMfaCode(MfaVerificationDTO dto);
    
    // MFA related methods
    ResponseEntity<?> initiateMfaSetup(String username);
    ResponseEntity<?> verifyAndEnableMfa(MfaSetupDTO setupDTO);
    ResponseEntity<?> disableMfa(MfaSetupDTO setupDTO);
    ResponseEntity<?> generateNewRecoveryCodes(MfaSetupDTO setupDTO);
    
    // Session validity management
    boolean updateSessionValidity(Long userId, Long sessionValidityMs);
} 