# Unified Logging Strategy - Industry Standard Implementation

## Overview
This document outlines the standardized logging strategy implemented across the GourmetGo food ordering application backend.

## Log Level Strategy

### ERROR Level
- **Critical system failures** that require immediate attention
- **Unrecoverable exceptions** that prevent normal operation
- **Performance issues** exceeding 5000ms (CRITICAL_OPERATION_THRESHOLD)
- **API responses** with 5xx status codes

**Examples:**
```java
logger.error("Critical performance issue: {} took {}ms", operation, executionTimeMs);
logger.error("API Error: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
```

### WARN Level
- **Recoverable errors** that don't stop operation
- **Performance degradation** between 1000-5000ms (SLOW_OPERATION_THRESHOLD)
- **Security events** requiring attention
- **API responses** with 4xx status codes
- **Business rule violations**

**Examples:**
```java
logger.warn("Slow operation detected: {} took {}ms", operation, executionTimeMs);
logger.warn("Security event: {} with context: {}", event, context);
logger.warn("API Client Error: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
```

### INFO Level
- **Business events** and successful operations
- **API responses** with 2xx/3xx status codes
- **Performance metrics** between 500-1000ms
- **Audit trail** events

**Examples:**
```java
logger.info("Business event: {} with context: {}", event, context);
logger.info("API Success: {} {} returned {} in {}ms", method, endpoint, statusCode, duration);
logger.info("Operation: {} completed in {}ms", operation, executionTimeMs);
```

### DEBUG Level
- **Method entry/exit** tracing
- **Performance metrics** under 500ms
- **Detailed execution flow** for troubleshooting

**Examples:**
```java
logger.debug("Entering method: {} with parameters: {}", methodName, params);
logger.debug("Operation: {} completed in {}ms", operation, executionTimeMs);
```

## Standardized Annotations

### @Slf4j Usage
All controllers and services must use `@Slf4j` annotation instead of manual Logger instantiation:

```java
@RestController
@Slf4j
public class ExampleController {
    // Use 'log' field directly - no manual Logger creation needed
}
```

## LoggingUtils Methods

### Method Entry/Exit
```java
LoggingUtils.logMethodEntry(log, "methodName", "param1", value1, "param2", value2);
LoggingUtils.logMethodExit(log, "methodName");
```

### Business Events
```java
LoggingUtils.logBusinessEvent(log, "EVENT_NAME", "key1", value1, "key2", value2);
```

### Security Events
```java
LoggingUtils.logSecurityEvent(log, "SECURITY_EVENT", "key1", value1, "key2", value2);
```

### Error Logging
```java
LoggingUtils.logError(log, "Error message", exception, "context1", value1, "context2", value2);
```

### Performance Tracking
```java
long startTime = System.currentTimeMillis();
// ... business logic ...
LoggingUtils.logPerformance(log, "operationName", System.currentTimeMillis() - startTime);
```

### API Call Logging
```java
LoggingUtils.logApiCall(log, "GET", "/api/v1/products", 200, 150);
```

## Observability Integration

### MDC Context
All logging methods automatically populate MDC with relevant context:
- `correlationId` - Request correlation ID
- `userId` - Authenticated user ID
- `businessEvent` - Business event name
- `eventType` - Event classification (BUSINESS, SECURITY)
- `executionTimeMs` - Operation duration
- `operation` - Operation name

### Health Monitoring
- Logging system health indicator available at `/actuator/health`
- Automatic health checks for logging subsystem

### Request Logging
- Automatic HTTP request logging (excluding sensitive data)
- Configurable via `CommonsRequestLoggingFilter`

## Security Considerations

### Sensitive Data Masking
Automatic masking of:
- Email addresses (shows username@***)
- Phone numbers (shows ***-***-****)
- Credit card numbers (shows ****-****-****-****)

### Data Protection
- Request bodies are NOT logged by default
- HTTP headers are NOT logged by default
- All context data is automatically masked

## Performance Thresholds

| Threshold | Duration | Log Level | Action Required |
|-----------|----------|-----------|-----------------|
| Fast | < 500ms | DEBUG | Normal operation |
| Normal | 500-1000ms | INFO | Monitor trends |
| Slow | 1000-5000ms | WARN | Investigate optimization |
| Critical | > 5000ms | ERROR | Immediate attention |

## Implementation Checklist

### Controllers
- [ ] Add `@Slf4j` annotation
- [ ] Remove manual Logger instantiation
- [ ] Add method entry logging with parameters
- [ ] Add performance tracking for all endpoints
- [ ] Add business event logging for successful operations
- [ ] Add error logging with context for exceptions
- [ ] Add security event logging for authentication issues

### Services
- [ ] Add `@Slf4j` annotation
- [ ] Remove manual Logger instantiation
- [ ] Add method entry logging for public methods
- [ ] Add performance tracking for complex operations
- [ ] Add business event logging for domain events
- [ ] Add comprehensive error context

### Configuration
- [ ] Verify logback-spring.xml configuration
- [ ] Enable observability endpoints
- [ ] Configure appropriate log levels per environment
- [ ] Set up log aggregation (ELK stack recommended)

## Monitoring and Alerting

### Key Metrics to Monitor
1. **Error Rate**: Percentage of ERROR level logs
2. **Performance Degradation**: Frequency of WARN level performance logs
3. **Security Events**: Count of security-related logs
4. **Business Event Trends**: Patterns in business operations

### Recommended Alerts
- ERROR logs exceeding threshold (> 1% of total logs)
- Critical performance issues (> 5000ms operations)
- Security events requiring investigation
- Logging system health failures

## Best Practices

1. **Consistent Formatting**: Use LoggingUtils methods for standardized output
2. **Contextual Information**: Always include relevant business context
3. **Performance Awareness**: Track execution time for all operations
4. **Security First**: Never log sensitive data without masking
5. **Structured Data**: Use key-value pairs for searchable context
6. **Appropriate Levels**: Follow the defined log level strategy
7. **Correlation**: Ensure all related logs share correlation IDs