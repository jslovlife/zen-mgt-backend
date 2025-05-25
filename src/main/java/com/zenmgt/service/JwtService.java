package com.zenmgt.service;

import com.zenmgt.entity.AuthUser;
import java.util.Date;
import java.util.function.Function;
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