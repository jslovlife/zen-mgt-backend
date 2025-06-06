package com.zenmgt.service;

import com.zenmgt.model.AuthUser;
import com.zenmgt.model.AuthUserDetail;
import com.zenmgt.repository.UserGroupMemberRepository;
import com.zenmgt.util.SecurityHashUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.zenmgt.repository.UserRepository;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${app.jwt.secret-key}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    
    @Autowired
    private UserGroupMemberRepository userGroupMemberRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SecurityHashUtil securityHashUtil;

    @Override
    public String generateToken(AuthUser user) {
        // Get user-specific session validity
        long customExpiration = getUserSessionValidity(user);
        return generateTokenWithCustomExpiration(user, customExpiration);
    }

    @Override
    public String generateTokenWithCustomExpiration(AuthUser user, long customExpirationMs) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // Add hashed user ID
        String hashedUserId = securityHashUtil.hashUserId(user.getId());
        extraClaims.put("huid", hashedUserId);
        
        // Get primary user group ID and hash it
        List<Long> userGroupIds = userGroupMemberRepository.findUserGroupIdsByUserId(user.getId());
        if (!userGroupIds.isEmpty()) {
            // Use the first user group as primary (in practice, this could be more sophisticated)
            Long primaryUserGroupId = userGroupIds.get(0);
            String hashedUserGroupId = securityHashUtil.hashUserGroupId(primaryUserGroupId);
            extraClaims.put("hgid", hashedUserGroupId);
        }
        
        return generateToken(extraClaims, user, customExpirationMs);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public String extractHashedUserId(String token) {
        return extractClaim(token, claims -> claims.get("huid", String.class));
    }

    @Override
    public String extractHashedUserGroupId(String token) {
        return extractClaim(token, claims -> claims.get("hgid", String.class));
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean isTokenValid(String token, AuthUser user) {
        final String username = extractUsername(token);
        final String hashedUserId = extractHashedUserId(token);
        
        // Verify username matches
        boolean usernameValid = username.equals(user.getUserCode());
        
        // Verify hashed user ID matches
        boolean userIdValid = securityHashUtil.verifyUserIdHash(user.getId(), hashedUserId);
        
        // Verify token is not expired
        boolean notExpired = !isTokenExpired(token);
        
        return usernameValid && userIdValid && notExpired;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(Map<String, Object> extraClaims, AuthUser user, long expirationMs) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUserCode())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get user-specific session validity or fall back to default
     */
    private long getUserSessionValidity(AuthUser user) {
        Long sessionValidity = userRepository.getSessionValidity(user.getId());
        
        if (sessionValidity != null) {
            return sessionValidity;
        }

        // Fall back to global JWT expiration if user-specific value is not set
        return jwtExpiration;
    }
} 