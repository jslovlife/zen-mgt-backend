package com.zenmgt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Token Blacklist Service Implementation
 * Uses in-memory storage for token blacklist
 * TODO: For production, consider using Redis or database for distributed systems
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);
    
    @Value("${app.jwt.secret-key}")
    private String jwtSecret;
    
    // In-memory blacklist: token -> expiration timestamp
    private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Override
    public void blacklistToken(String token) {
        try {
            long expiration = getTokenExpiration(token);
            blacklistedTokens.put(token, expiration);
            logger.info("Token blacklisted successfully. Active blacklisted tokens: {}", blacklistedTokens.size());
        } catch (Exception e) {
            logger.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }
    
    @Override
    public long getTokenExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
            
            Date expiration = claims.getExpiration();
            return expiration != null ? expiration.getTime() : 0;
        } catch (Exception e) {
            logger.error("Error parsing token expiration: {}", e.getMessage());
            return 0;
        }
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        
        int removedCount = initialSize - blacklistedTokens.size();
        if (removedCount > 0) {
            logger.info("Cleaned up {} expired blacklisted tokens. Remaining: {}", 
                removedCount, blacklistedTokens.size());
        }
    }
} 