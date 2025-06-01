package com.zenmgt.controller;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mgt/v1/users")
@RequiredArgsConstructor
public class AuthUserController {

    @Autowired
    private final AuthUserService authUserService;
    
    @GetMapping
    public ResponseEntity<List<AuthUserDTO>> getAllUsers() {
        return ResponseEntity.ok(authUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthUserDTO> getUserById(@PathVariable Long id) {
        return authUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{userCode}")
    public ResponseEntity<AuthUserDTO> getUserByCode(@PathVariable String userCode) {
        return authUserService.getUserByCode(userCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody AuthUserDTO userDTO) {
        try {
            AuthUserDTO savedUser = authUserService.createUser(userDTO);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AuthUserDTO userDTO) {
        try {
            AuthUserDTO updatedUser = authUserService.updateUser(id, userDTO);
            if (updatedUser == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (authUserService.deleteUser(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<AuthUserDTO>> getUserList() {
        return ResponseEntity.ok(authUserService.getAllUsers());
    }

    /**
     * Update session validity for a specific user
     * @param id User ID
     * @param sessionValidityMs Session validity in milliseconds
     * @return Success/error response
     */
    @PutMapping("/{id}/session-validity")
    @Transactional
    public ResponseEntity<?> updateSessionValidity(
            @PathVariable Long id, 
            @RequestBody Map<String, Long> request) {
        try {
            Long sessionValidityMs = request.get("sessionValidityMs");
            if (sessionValidityMs == null || sessionValidityMs <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid session validity value"));
            }
            
            boolean updated = authUserService.updateSessionValidity(id, sessionValidityMs);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                    "message", "Session validity updated successfully",
                    "sessionValidityMs", sessionValidityMs,
                    "sessionValidityHours", sessionValidityMs / (60 * 60 * 1000.0)
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error updating session validity: " + e.getMessage()));
        }
    }
} 