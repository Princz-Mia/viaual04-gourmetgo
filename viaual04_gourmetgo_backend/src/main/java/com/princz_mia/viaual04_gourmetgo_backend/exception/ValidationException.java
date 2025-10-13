package com.princz_mia.viaual04_gourmetgo_backend.exception;

import java.util.List;

public class ValidationException extends AppException {

    private final List<ErrorResponse.ValidationError> validationErrors;

    public ValidationException(String message, List<ErrorResponse.ValidationError> validationErrors) {
        super(message, ErrorType.VALIDATION_ERROR);
        this.validationErrors = validationErrors;
    }

    public List<ErrorResponse.ValidationError> getValidationErrors() {
        return validationErrors;
    }
}