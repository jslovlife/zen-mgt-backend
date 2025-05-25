package com.zenmgt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zenmgt.service.AuthUserService;
import com.zenmgt.dto.MfaSetupDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mgt/v1/mfa")
@RequiredArgsConstructor
@CrossOrigin
public class MfaController {
    private final AuthUserService authUserService;

    @GetMapping("/setup/init")
    public ResponseEntity<?> initiateMfaSetup(@RequestParam String username) {
        return authUserService.initiateMfaSetup(username);
    }

    @PostMapping("/setup/verify")
    public ResponseEntity<?> verifyAndEnableMfa(@RequestBody MfaSetupDTO setupDTO) {
        return authUserService.verifyAndEnableMfa(setupDTO);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaSetupDTO setupDTO) {
        return authUserService.verifyAndEnableMfa(setupDTO);
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableMfa(@RequestBody MfaSetupDTO setupDTO) {
        return authUserService.disableMfa(setupDTO);
    }

    @PostMapping("/recovery-codes/generate")
    public ResponseEntity<?> generateNewRecoveryCodes(@RequestBody MfaSetupDTO setupDTO) {
        return authUserService.generateNewRecoveryCodes(setupDTO);
    }
} 