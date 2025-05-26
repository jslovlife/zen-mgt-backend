package com.zenmgt.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Utility class for managing trace IDs in MDC (Mapped Diagnostic Context)
 * for thread-specific logging and request tracking.
 * Uses UUID for generating unique trace IDs.
 */
@Component
public class TraceIdUtil {
    
    public static final String TRACE_ID_KEY = "traceId";
    public static final String USER_ID_KEY = "userId";
    public static final String REQUEST_ID_KEY = "requestId";
    
    /**
     * Generate a new trace ID and set it in MDC
     * @return the generated trace ID
     */
    public static String generateAndSetTraceId() {
        String traceId = generateTraceId();
        setTraceId(traceId);
        return traceId;
    }
    
    /**
     * Generate a new trace ID using UUID
     * @return a new UUID-based trace ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Set trace ID in MDC
     * @param traceId the trace ID to set
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }
    
    /**
     * Get current trace ID from MDC
     * @return current trace ID or null if not set
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * Set user ID in MDC for user-specific logging
     * @param userId the user ID to set
     */
    public static void setUserId(String userId) {
        MDC.put(USER_ID_KEY, userId);
    }
    
    /**
     * Get current user ID from MDC
     * @return current user ID or null if not set
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }
    
    /**
     * Set request ID in MDC for request-specific logging
     * @param requestId the request ID to set
     */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }
    
    /**
     * Get current request ID from MDC
     * @return current request ID or null if not set
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
    
    /**
     * Clear all MDC values for the current thread
     */
    public static void clear() {
        MDC.clear();
    }
    
    /**
     * Clear specific key from MDC
     * @param key the key to remove
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
    
    /**
     * Clear trace ID from MDC
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * Clear user ID from MDC
     */
    public static void clearUserId() {
        MDC.remove(USER_ID_KEY);
    }
    
    /**
     * Clear request ID from MDC
     */
    public static void clearRequestId() {
        MDC.remove(REQUEST_ID_KEY);
    }
    
    /**
     * Execute a runnable with a specific trace ID
     * @param traceId the trace ID to use
     * @param runnable the code to execute
     */
    public static void executeWithTraceId(String traceId, Runnable runnable) {
        String originalTraceId = getTraceId();
        try {
            setTraceId(traceId);
            runnable.run();
        } finally {
            if (originalTraceId != null) {
                setTraceId(originalTraceId);
            } else {
                clearTraceId();
            }
        }
    }
    
    /**
     * Execute a runnable with a new generated trace ID
     * @param runnable the code to execute
     * @return the generated trace ID
     */
    public static String executeWithNewTraceId(Runnable runnable) {
        String traceId = generateTraceId();
        executeWithTraceId(traceId, runnable);
        return traceId;
    }
} 