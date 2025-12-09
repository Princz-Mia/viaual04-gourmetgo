package com.princz_mia.viaual04_gourmetgo_backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS("ALREADY_EXISTS", HttpStatus.CONFLICT),
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", HttpStatus.CONFLICT),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", HttpStatus.LOCKED),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final HttpStatus httpStatus;
    
    ErrorType(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
    
    public String getCode() {
        return code;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}