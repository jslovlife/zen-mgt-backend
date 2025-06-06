package com.zenmgt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.MediaType;

import com.zenmgt.service.JwtService;
import com.zenmgt.repository.UserRepository;
import com.zenmgt.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TokenBlacklistService tokenBlacklistService;

    private final List<String> PUBLIC_PATHS = Arrays.asList(
        "/mgt/v1/auth/login",
        "/mgt/v1/auth/oauth2/login",
        "/mgt/v1/auth/oauth2/login/success",
        "/mgt/v1/auth/oauth2/login/failure",
        "/mgt/v1/auth/config",
        "/error",
        // Test endpoints for debugging
        "/test/**",
        "/mgt/v1/enums/test",
        "/mgt/v1/enums/test-all",
        "/mgt/v1/users/test-search",
        // Swagger/OpenAPI paths
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/webjars/**"
    );

    private final List<String> MFA_PUBLIC_PATHS = Arrays.asList(
        "/mgt/v1/mfa/setup/init",
        "/mgt/v1/mfa/setup/init/**",
        "/mgt/v1/mfa/setup/verify",
        "/mgt/v1/mfa/verify"
    );

    private void sendErrorResponse(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.getWriter().write(String.format(
            "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
            message,
            path
        ));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        final String path;
        if (contextPath != null && !contextPath.isEmpty() && request.getRequestURI().startsWith(contextPath)) {
            path = request.getRequestURI().substring(contextPath.length());
        } else {
            path = request.getRequestURI();
        }
        
        logger.debug("Checking path after context path removal: {}", path);
        
        // Explicit check for MFA setup init endpoint
        if (path.equals("/mgt/v1/mfa/setup/init") || path.startsWith("/mgt/v1/mfa/setup/init?")) {
            logger.debug("Path '{}' is MFA setup init endpoint - skipping JWT filter", path);
            return true;
        }
        
        // Check MFA paths with pattern matching
        boolean isMfaPath = MFA_PUBLIC_PATHS.stream()
                .anyMatch(mfaPath -> {
                    AntPathRequestMatcher matcher = new AntPathRequestMatcher(mfaPath);
                    boolean matches = matcher.matches(request);
                    if (matches) {
                        logger.debug("Path '{}' matches MFA pattern '{}' - skipping JWT filter", path, mfaPath);
                    }
                    return matches;
                });

        if (isMfaPath) {
            return true;
        }

        // Then check other public paths
        boolean isPublicPath = PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> {
                    AntPathRequestMatcher matcher = new AntPathRequestMatcher(publicPath);
                    boolean matches = matcher.matches(request);
                    if (matches) {
                        logger.debug("Path '{}' matches public pattern '{}' - skipping JWT filter", path, publicPath);
                    }
                    return matches;
                });
        
        logger.debug("Final shouldNotFilter result for path '{}': {}", path, isPublicPath);
        return isPublicPath;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (shouldNotFilter(request)) {
                logger.debug("Skipping JWT authentication for public path: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            logger.debug("Authorization header: {}", authHeader != null ? "present" : "absent");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("No valid Bearer token found");
                sendErrorResponse(response, "No valid Bearer token found", request.getRequestURI());
                return;
            }

            final String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            logger.debug("Extracted username from JWT: {}", username);

            // Check if token is blacklisted (logged out)
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                logger.debug("Token is blacklisted - user has logged out");
                sendErrorResponse(response, "Token has been invalidated", request.getRequestURI());
                return;
            }

            final String hashedUserId = jwtService.extractHashedUserId(jwt);
            final String hashedUserGroupId = jwtService.extractHashedUserGroupId(jwt);
            
            logger.debug("Extracted hashed user ID: {}", hashedUserId != null ? "present" : "absent");
            logger.debug("Extracted hashed user group ID: {}", hashedUserGroupId != null ? "present" : "absent");

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userOptional = userRepository.findByUsername(username);
                
                if (userOptional.isPresent() && jwtService.isTokenValid(jwt, userOptional.get())) {
                    var user = userOptional.get();
                    var authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Successfully authenticated user: {}", username);
                    filterChain.doFilter(request, response);
                } else {
                    logger.debug("User not found or token invalid for username: {}", username);
                    sendErrorResponse(response, "Invalid authentication token", request.getRequestURI());
                }
            } else {
                logger.debug("No username in token or authentication already set");
                sendErrorResponse(response, "Invalid authentication token", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            sendErrorResponse(response, "Authentication failed", request.getRequestURI());
        }
    }
} 