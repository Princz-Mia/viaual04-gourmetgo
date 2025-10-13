package com.princz_mia.viaual04_gourmetgo_backend.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorType.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, ErrorType.RESOURCE_NOT_FOUND, cause);
    }
}
