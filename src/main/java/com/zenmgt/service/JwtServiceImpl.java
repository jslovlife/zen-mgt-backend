package com.zenmgt.service;

import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserDetail;
import com.zenmgt.repository.AuthUserDetailRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret-key}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    private AuthUserDetailRepository authUserDetailRepository;

    @Override
    public String generateToken(AuthUser user) {
        // Get user-specific session validity
        long customExpiration = getUserSessionValidity(user);
        return generateTokenWithCustomExpiration(user, customExpiration);
    }

    @Override
    public String generateTokenWithCustomExpiration(AuthUser user, long customExpirationMs) {
        return generateToken(new HashMap<>(), user, customExpirationMs);
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
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean isTokenValid(String token, AuthUser user) {
        final String username = extractUsername(token);
        return username.equals(user.getUserCode()) && !isTokenExpired(token);
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
        Optional<AuthUserDetail> userDetailOpt = authUserDetailRepository.findByParentId(user.getId());
        if (userDetailOpt.isPresent() && userDetailOpt.get().getSessionValidity() != null) {
            return userDetailOpt.get().getSessionValidity();
        }
        // Fall back to global JWT expiration if user-specific value is not set
        return jwtExpiration;
    }
} 