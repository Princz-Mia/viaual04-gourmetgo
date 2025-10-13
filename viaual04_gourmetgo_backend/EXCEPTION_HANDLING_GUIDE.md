# Unified Exception Handling Guide

## Overview
This application implements a comprehensive, industry-standard exception handling system that provides consistent error responses across all endpoints.

## Architecture Components

### 1. ErrorResponse Model
- **Location**: `com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorResponse`
- **Purpose**: Unified error response structure for all API endpoints
- **Features**:
  - Consistent error format
  - Trace ID for debugging
  - Validation error details
  - Timestamp and request path tracking

### 2. ErrorType Enum
- **Location**: `com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType`
- **Purpose**: Standardized error classification with HTTP status mapping
- **Types**:
  - `RESOURCE_NOT_FOUND` → 404
  - `RESOURCE_ALREADY_EXISTS` → 409
  - `VALIDATION_ERROR` → 400
  - `AUTHENTICATION_ERROR` → 401
  - `AUTHORIZATION_ERROR` → 403
  - `ACCOUNT_LOCKED` → 423
  - `BUSINESS_RULE_VIOLATION` → 400
  - `EXTERNAL_SERVICE_ERROR` → 503
  - `INTERNAL_SERVER_ERROR` → 500

### 3. Exception Hierarchy
- **Base**: `AppException` - All custom exceptions extend this
- **Specific Exceptions**:
  - `ResourceNotFoundException` - For missing resources
  - `AlreadyExistsException` - For duplicate resources
  - `ValidationException` - For validation errors with details
  - `BusinessRuleException` - For business logic violations

### 4. Global Exception Handler
- **Location**: `com.princz_mia.viaual04_gourmetgo_backend.exception.RestExceptionHandler`
- **Features**:
  - Handles all exception types
  - Generates trace IDs for debugging
  - Comprehensive logging
  - Consistent error response format

## Usage Guidelines

### For Service Layer
```java
// Throwing exceptions
throw new ResourceNotFoundException("Customer not found with ID: " + id);
throw new AlreadyExistsException("Customer already exists with email: " + email);
throw new BusinessRuleException("Cannot delete customer with active orders");

// Exception chaining
throw new ResourceNotFoundException("Customer not found", originalException);
```

### For Controller Layer
```java
// Clean controllers - no try-catch blocks needed
@GetMapping("/{id}")
public ResponseEntity<ApiResponse> getCustomer(@PathVariable UUID id) {
    Customer customer = customerService.getCustomerById(id); // Exception handled globally
    CustomerDto dto = customerService.convertCustomerToDto(customer);
    return ResponseEntity.ok(new ApiResponse("Customer retrieved successfully", dto));
}
```

### Error Response Format
```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Customer not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "status": 404,
  "path": "/api/v1/customers/123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2024-01-15 10:30:45",
  "traceId": "a1b2c3d4"
}
```

### Validation Error Response
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "status": 400,
  "path": "/api/v1/customers",
  "timestamp": "2024-01-15 10:30:45",
  "traceId": "a1b2c3d4",
  "validationErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email should be valid"
    }
  ]
}
```

## Benefits

1. **Consistency**: All endpoints return the same error format
2. **Maintainability**: Centralized exception handling logic
3. **Debugging**: Trace IDs and comprehensive logging
4. **Client Experience**: Predictable error responses
5. **Security**: Controlled error information exposure
6. **Scalability**: Easy to extend for new exception types

## Migration Notes

- All controllers have been cleaned of manual exception handling
- Service layer uses new exception structure with better messages
- Duplicate ErrorDto classes have been removed
- All exceptions now use ErrorType enum for consistency