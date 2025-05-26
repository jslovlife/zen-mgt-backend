package com.zenmgt.filter;

import com.zenmgt.util.TraceIdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to automatically generate and manage trace IDs for incoming HTTP requests.
 * This filter runs early in the filter chain to ensure trace IDs are available
 * for all subsequent processing.
 */
@Component
@Order(1) // Run this filter early in the chain
public class TraceIdFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract trace ID
            String traceId = extractOrGenerateTraceId(httpRequest);
            String requestId = extractOrGenerateRequestId(httpRequest);
            
            // Set trace ID and request ID in MDC
            TraceIdUtil.setTraceId(traceId);
            TraceIdUtil.setRequestId(requestId);
            
            // Add trace ID to response headers for client tracking
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
            
            // Log request start
            logger.debug("Request started: {} {} - TraceId: {}, RequestId: {}", 
                httpRequest.getMethod(), 
                httpRequest.getRequestURI(), 
                traceId, 
                requestId);
            
            // Continue with the filter chain
            chain.doFilter(request, response);
            
            // Log request completion
            logger.debug("Request completed: {} {} - TraceId: {}, Status: {}", 
                httpRequest.getMethod(), 
                httpRequest.getRequestURI(), 
                traceId, 
                httpResponse.getStatus());
                
        } finally {
            // Clean up MDC to prevent memory leaks
            TraceIdUtil.clear();
        }
    }
    
    /**
     * Extract trace ID from request header or generate a new one
     */
    private String extractOrGenerateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = TraceIdUtil.generateTraceId();
        }
        return traceId;
    }
    
    /**
     * Extract request ID from request header or generate a new one
     */
    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        return requestId;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("TraceIdFilter initialized");
    }
    
    @Override
    public void destroy() {
        logger.info("TraceIdFilter destroyed");
    }
} 