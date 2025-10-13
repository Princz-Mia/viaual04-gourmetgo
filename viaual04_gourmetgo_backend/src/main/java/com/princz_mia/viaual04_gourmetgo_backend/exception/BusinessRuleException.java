package com.princz_mia.viaual04_gourmetgo_backend.exception;

public class BusinessRuleException extends AppException {

    public BusinessRuleException(String message) {
        super(message, ErrorType.BUSINESS_RULE_VIOLATION);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, ErrorType.BUSINESS_RULE_VIOLATION, cause);
    }
}