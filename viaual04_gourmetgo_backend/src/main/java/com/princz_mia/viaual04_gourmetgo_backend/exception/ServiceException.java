package com.princz_mia.viaual04_gourmetgo_backend.exception;

public class ServiceException extends AppException {
    public ServiceException(String message, ErrorType errorType) {
        super(message, errorType);
    }
    
    public ServiceException(String message, ErrorType errorType, Throwable cause) {
        super(message, errorType, cause);
    }
}