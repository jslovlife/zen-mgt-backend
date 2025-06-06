package com.zenmgt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Alternative JWT utility for direct token parsing
 * Works with hashed user IDs for enhanced security
 */
@Component
public class JwtUtil {
    
    @Value("${app.jwt.secret:your-secret-key}")
    private String jwtSecret;
    
    /**
     * Extract current hashed user ID from Authorization header
     * @param request HTTP request containing JWT token
     * @return Hashed user ID or null if not found/invalid
     */
    public String getCurrentHashedUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return null;
            
            Claims claims = parseToken(token);
            if (claims == null) return null;
            
            // Try different claim names for hashed user ID
            String hashedUserId = (String) claims.get("hashedUserId");
            if (hashedUserId != null && !hashedUserId.matches("^\\d+$")) { // Not a raw number
                return hashedUserId;
            }
            
            hashedUserId = (String) claims.get("hashed_user_id");
            if (hashedUserId != null && !hashedUserId.matches("^\\d+$")) {
                return hashedUserId;
            }
            
            // Check 'sub' claim if it contains hashed ID
            hashedUserId = claims.getSubject();
            if (hashedUserId != null && !hashedUserId.matches("^\\d+$")) {
                return hashedUserId;
            }
            
            // Check 'userId' claim but only if it's not a raw number
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof String) {
                String userIdStr = (String) userIdObj;
                if (!userIdStr.matches("^\\d+$")) { // Not a raw number
                    return userIdStr;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract JWT token from Authorization header
     * @param request HTTP request
     * @return JWT token or null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * Parse JWT token and extract claims
     * @param token JWT token
     * @return Claims or null if invalid
     */
    private Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get username from JWT token
     * @param request HTTP request
     * @return Username or null
     */
    public String getUsernameFromRequest(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return null;
            
            Claims claims = parseToken(token);
            if (claims == null) return null;
            
            // Try different claim names for username
            String username = (String) claims.get("username");
            if (username != null) return username;
            
            username = (String) claims.get("preferred_username");
            if (username != null) return username;
            
            username = (String) claims.get("name");
            if (username != null) return username;
            
            // Fallback to subject if it's not a hashed ID
            String subject = claims.getSubject();
            if (subject != null && subject.matches("^[a-zA-Z][a-zA-Z0-9._-]*$")) { // Looks like username
                return subject;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
} 