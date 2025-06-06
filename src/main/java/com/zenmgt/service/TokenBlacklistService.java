package com.zenmgt.service;

/**
 * Service for managing blacklisted/revoked JWT tokens
 * This ensures that logged out tokens cannot be used even if they haven't expired
 */
public interface TokenBlacklistService {
    
    /**
     * Add token to blacklist (revoke it)
     * @param token The JWT token to blacklist
     */
    void blacklistToken(String token);
    
    /**
     * Check if token is blacklisted
     * @param token The JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    boolean isTokenBlacklisted(String token);
    
    /**
     * Remove expired tokens from blacklist (cleanup)
     */
    void cleanupExpiredTokens();
    
    /**
     * Get token expiration time from JWT
     * @param token The JWT token
     * @return expiration timestamp
     */
    long getTokenExpiration(String token);
} 