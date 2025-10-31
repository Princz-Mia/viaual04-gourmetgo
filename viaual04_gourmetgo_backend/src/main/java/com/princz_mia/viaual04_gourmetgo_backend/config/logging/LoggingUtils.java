package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public final class LoggingUtils {

    // Log Level Strategy Constants
    private static final long SLOW_OPERATION_THRESHOLD = 1000L;
    private static final long CRITICAL_OPERATION_THRESHOLD = 5000L;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{3}-?\\d{3}-?\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    private LoggingUtils() {}

    public static String maskSensitiveData(String input) {
        if (input == null) return null;
        
        String masked = input;
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("$1@***");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("***-***-****");
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("****-****-****-****");
        
        return masked;
    }

    public static void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering method: {} with parameters: {}", methodName, maskParameters(params));
        }
    }

    public static void logMethodExit(Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {} with result: {}", methodName, maskSensitiveData(String.valueOf(result)));
        }
    }

    public static void logMethodExit(Logger logger, String methodName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {}", methodName);
        }
    }

    // Overloaded method for varargs support
    public static void logBusinessEvent(Logger logger, String event, Object... keyValuePairs) {
        Map<String, Object> context = createContextMap(keyValuePairs);
        logBusinessEvent(logger, event, context);
    }
    
    public static void logBusinessEvent(Logger logger, String event, Map<String, Object> context) {
        MDC.put("businessEvent", event);
        MDC.put("eventType", "BUSINESS");
        try {
            logger.info("Business event: {} with context: {}", event, maskContext(context));
        } finally {
            MDC.remove("businessEvent");
            MDC.remove("eventType");
        }
    }
    
    public static void logSecurityEvent(Logger logger, String event, Object... keyValuePairs) {
        Map<String, Object> context = createContextMap(keyValuePairs);
        MDC.put("securityEvent", event);
        MDC.put("eventType", "SECURITY");
        try {
            logger.warn("Security event: {} with context: {}", event, maskContext(context));
        } finally {
            MDC.remove("securityEvent");
            MDC.remove("eventType");
        }
    }
    
    public static void logError(Logger logger, String message, Throwable throwable, Object... keyValuePairs) {
        Map<String, Object> context = createContextMap(keyValuePairs);
        MDC.put("errorContext", context.toString());
        try {
            logger.error("{} - Context: {}", message, maskContext(context), throwable);
        } finally {
            MDC.remove("errorContext");
        }
    }

    public static void logPerformance(Logger logger, String operation, long executionTimeMs) {
        MDC.put("executionTimeMs", String.valueOf(executionTimeMs));
        MDC.put("operation", operation);
        try {
            if (executionTimeMs > CRITICAL_OPERATION_THRESHOLD) {
                logger.error("Critical performance issue: {} took {}ms", operation, executionTimeMs);
            } else if (executionTimeMs > SLOW_OPERATION_THRESHOLD) {
                logger.warn("Slow operation detected: {} took {}ms", operation, executionTimeMs);
            } else if (executionTimeMs > 500) {
                logger.info("Operation: {} completed in {}ms", operation, executionTimeMs);
            } else {
                logger.debug("Operation: {} completed in {}ms", operation, executionTimeMs);
            }
        } finally {
            MDC.remove("executionTimeMs");
            MDC.remove("operation");
        }
    }
    
    public static void logApiCall(Logger logger, String method, String endpoint, int statusCode, long duration) {
        MDC.put("httpMethod", method);
        MDC.put("endpoint", endpoint);
        MDC.put("statusCode", String.valueOf(statusCode));
        MDC.put("duration", String.valueOf(duration));
        try {
            if (statusCode >= 500) {
                logger.error("API Error: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
            } else if (statusCode >= 400) {
                logger.warn("API Client Error: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
            } else {
                logger.info("API Success: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
            }
        } finally {
            MDC.remove("httpMethod");
            MDC.remove("endpoint");
            MDC.remove("statusCode");
            MDC.remove("duration");
        }
    }

    private static Object[] maskParameters(Object[] params) {
        if (params == null) return new Object[0];
        
        Object[] masked = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            masked[i] = params[i] != null ? maskSensitiveData(params[i].toString()) : null;
        }
        return masked;
    }

    private static Map<String, Object> createContextMap(Object... keyValuePairs) {
        Map<String, Object> context = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            String key = keyValuePairs[i].toString();
            Object value = keyValuePairs[i + 1];
            context.put(key, value);
        }
        return context;
    }
    
    private static Map<String, Object> maskContext(Map<String, Object> context) {
        if (context == null) return Map.of();
        
        return context.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() != null ? 
                    maskSensitiveData(entry.getValue().toString()) : null
            ));
    }
}