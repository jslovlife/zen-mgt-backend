package com.zenmgt.controller;

import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(value = "/mgt/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthUserService authUserService;

    @Value("${spring.security.oauth2.enabled:false}")
    private boolean oauth2Enabled;

    @Value("${app.auth.password-auth-enabled:true}")
    private boolean passwordAuthEnabled;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAuthConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("oauth2Enabled", oauth2Enabled);
        config.put("passwordAuthEnabled", passwordAuthEnabled);
        if (oauth2Enabled) {
            config.put("googleAuthUrl", "/oauth2/authorization/google");
        }
        return ResponseEntity.ok(config);
    }

    @GetMapping("/oauth2/login")
    public ResponseEntity<?> oauthLogin() {
        if (!oauth2Enabled) {
            return ResponseEntity.badRequest().body(Map.of("error", "OAuth2 authentication is disabled"));
        }
        Map<String, String> response = new HashMap<>();
        response.put("googleAuthUrl", "/oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            return authUserService.authenticateUser(loginRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/mfa/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyMfa(@RequestBody MfaVerificationDTO verificationDTO) {
        try {
            return authUserService.verifyMfaCode(verificationDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/oauth2/login/success")
    public ResponseEntity<?> loginSuccess(OAuth2AuthenticationToken authentication) {
        if (!oauth2Enabled) {
            return ResponseEntity.badRequest().body(Map.of("error", "OAuth2 authentication is disabled"));
        }
        try {
            OAuth2User user = authentication.getPrincipal();
            String email = user.getAttribute("email");
            String name = user.getAttribute("name");
            String picture = user.getAttribute("picture");
            return authUserService.handleOAuth2Login(email, name, picture);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/oauth2/login/failure")
    public ResponseEntity<Map<String, String>> loginFailure() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Login failed");
        return ResponseEntity.badRequest().body(response);
    }

   
    
    @PostMapping(value = "/mfa/setup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> setupMfa() {
        if (!passwordAuthEnabled) {
            return ResponseEntity.badRequest().body(Map.of("error", "MFA setup is only available for password authentication"));
        }
        try {
            return authUserService.setupMfa();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 