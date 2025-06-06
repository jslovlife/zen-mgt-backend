package com.zenmgt.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Component
public class SecurityHashUtil {
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String USER_PREFIX = "USER_";
    private static final String GROUP_PREFIX = "GROUP_";
    
    @Value("${app.jwt.secret-key}")
    private String secretKey;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Hash a user ID for security purposes
     * @param userId The user ID to hash
     * @return Base64 encoded hash
     */
    public String hashUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return generateHmacHash(USER_PREFIX + userId);
    }
    
    /**
     * Hash a user group ID for security purposes
     * @param userGroupId The user group ID to hash
     * @return Base64 encoded hash
     */
    public String hashUserGroupId(Long userGroupId) {
        if (userGroupId == null) {
            return null;
        }
        return generateHmacHash(GROUP_PREFIX + userGroupId);
    }
    
    /**
     * Decode a hashed user ID back to the original user ID
     * @param hashedUserId The hashed user ID from frontend
     * @return The original user ID, or null if invalid
     */
    public Long decodeHashedUserId(String hashedUserId) {
        if (hashedUserId == null || hashedUserId.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Get all user IDs from database and check against the hash
            List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT id FROM auth_user WHERE record_status IN (0, 1, 2, 3, 4)", 
                Long.class
            );
            
            for (Long userId : userIds) {
                String expectedHash = hashUserId(userId);
                if (hashedUserId.equals(expectedHash)) {
                    return userId;
                }
            }
        } catch (Exception e) {
            // Log error but don't expose it
            System.err.println("Error decoding hashed user ID: " + e.getMessage());
        }
        
        return null; // Hash not found or invalid
    }
    
    /**
     * Decode a hashed user group ID back to the original user group ID
     * @param hashedUserGroupId The hashed user group ID from frontend
     * @return The original user group ID, or null if invalid
     */
    public Long decodeHashedUserGroupId(String hashedUserGroupId) {
        if (hashedUserGroupId == null || hashedUserGroupId.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Get all user group IDs from database and check against the hash
            List<Long> groupIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT user_group_id FROM user_group_member", 
                Long.class
            );
            
            for (Long groupId : groupIds) {
                String expectedHash = hashUserGroupId(groupId);
                if (hashedUserGroupId.equals(expectedHash)) {
                    return groupId;
                }
            }
        } catch (Exception e) {
            // Log error but don't expose it
            System.err.println("Error decoding hashed user group ID: " + e.getMessage());
        }
        
        return null; // Hash not found or invalid
    }
    
    /**
     * Verify if a hash matches the original user ID
     * @param userId The original user ID
     * @param hash The hash to verify
     * @return true if hash matches, false otherwise
     */
    public boolean verifyUserIdHash(Long userId, String hash) {
        if (userId == null || hash == null) {
            return false;
        }
        String expectedHash = hashUserId(userId);
        return expectedHash.equals(hash);
    }
    
    /**
     * Verify if a hash matches the original user group ID
     * @param userGroupId The original user group ID
     * @param hash The hash to verify
     * @return true if hash matches, false otherwise
     */
    public boolean verifyUserGroupIdHash(Long userGroupId, String hash) {
        if (userGroupId == null || hash == null) {
            return false;
        }
        String expectedHash = hashUserGroupId(userGroupId);
        return expectedHash.equals(hash);
    }
    
    /**
     * Validate that a hashed user ID is properly formatted and potentially valid
     * @param hashedUserId The hashed user ID to validate
     * @return true if format is valid, false otherwise
     */
    public boolean isValidHashedUserId(String hashedUserId) {
        if (hashedUserId == null || hashedUserId.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Check if it's valid URL-safe Base64
            Base64.getUrlDecoder().decode(hashedUserId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Validate that a hashed user group ID is properly formatted and potentially valid
     * @param hashedUserGroupId The hashed user group ID to validate
     * @return true if format is valid, false otherwise
     */
    public boolean isValidHashedUserGroupId(String hashedUserGroupId) {
        if (hashedUserGroupId == null || hashedUserGroupId.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Check if it's valid URL-safe Base64
            Base64.getUrlDecoder().decode(hashedUserGroupId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Generate HMAC-SHA256 hash for the given data
     * @param data The data to hash
     * @return URL-safe Base64 encoded hash (replaces / with -, + with _, removes = padding)
     */
    private String generateHmacHash(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMAC hash", e);
        }
    }
} 