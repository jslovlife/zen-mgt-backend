package com.zenmgt.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility to extract current user information from Spring Security context
 * Works with hashed user IDs for enhanced security
 */
@Component
public class SecurityContextUtil {
    
    /**
     * Get current hashed user ID from JWT token in security context
     * @return Current hashed user ID or null if not authenticated
     */
    public String getCurrentHashedUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            // If using JWT
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                // JWT token should contain hashed user ID for security
                String hashedUserId = jwt.getClaimAsString("hashedUserId");
                if (hashedUserId != null) return hashedUserId;
                
                // Fallback to 'sub' claim if it contains hashed ID
                String subject = jwt.getClaimAsString("sub");
                if (subject != null && !subject.matches("^\\d+$")) { // Not a raw number
                    return subject;
                }
                
                // Try alternative claim names
                hashedUserId = jwt.getClaimAsString("hashed_user_id");
                if (hashedUserId != null) return hashedUserId;
                
                hashedUserId = jwt.getClaimAsString("userId");
                if (hashedUserId != null && !hashedUserId.matches("^\\d+$")) { // Not a raw number
                    return hashedUserId;
                }
            }
            
            // If using custom UserDetails, assume username is hashed ID
            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = authentication.getName();
                // Only return if it doesn't look like a raw number
                if (username != null && !username.matches("^\\d+$")) {
                    return username;
                }
            }
            
            // Fallback: if authentication name is not a raw number, assume it's hashed
            String authName = authentication.getName();
            if (authName != null && !authName.matches("^\\d+$")) {
                return authName;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get current username from security context
     * @return Current username or null if not authenticated
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            // If using JWT, try to get username claim
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String username = jwt.getClaimAsString("username");
                if (username != null) return username;
                
                username = jwt.getClaimAsString("preferred_username");
                if (username != null) return username;
                
                username = jwt.getClaimAsString("name");
                if (username != null) return username;
            }
            
            return authentication.getName();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if current user is authenticated
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        } catch (Exception e) {
            return false;
        }
    }
} 