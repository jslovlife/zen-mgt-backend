package com.zenmgt.service;

import java.util.Date;
import java.util.function.Function;

import com.zenmgt.model.AuthUser;

import io.jsonwebtoken.Claims;

/**
 * Service interface for JWT (JSON Web Token) operations.
 */
public interface JwtService {
    /**
     * Generates a JWT token for a user.
     *
     * @param user The user for whom to generate the token
     * @return The generated JWT token
     */
    String generateToken(AuthUser user);

    /**
     * Generates a JWT token for a user with custom expiration duration.
     *
     * @param user The user for whom to generate the token
     * @param customExpirationMs Custom expiration duration in milliseconds
     * @return The generated JWT token
     */
    String generateTokenWithCustomExpiration(AuthUser user, long customExpirationMs);

    /**
     * Extracts the username from a JWT token.
     *
     * @param token The JWT token
     * @return The username
     */
    String extractUsername(String token);

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token
     * @return The expiration date
     */
    Date extractExpiration(String token);

    /**
     * Extracts the hashed user ID from a JWT token.
     *
     * @param token The JWT token
     * @return The hashed user ID
     */
    String extractHashedUserId(String token);

    /**
     * Extracts the hashed user group ID from a JWT token.
     *
     * @param token The JWT token
     * @return The hashed user group ID
     */
    String extractHashedUserGroupId(String token);

    /**
     * Extracts a claim from a JWT token using a claims resolver function.
     *
     * @param token The JWT token
     * @param claimsResolver The function to resolve the claim
     * @return The resolved claim value
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Validates a JWT token for a user.
     *
     * @param token The JWT token to validate
     * @param user The user to validate against
     * @return true if the token is valid, false otherwise
     */
    boolean isTokenValid(String token, AuthUser user);
} 