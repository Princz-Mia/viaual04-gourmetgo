package com.princz_mia.viaual04_gourmetgo_backend.exception;

public class AppException extends RuntimeException {

    private final ErrorType errorType;

    public AppException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public AppException(String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public AppException(String message) {
        super(message);
        this.errorType = ErrorType.INTERNAL_SERVER_ERROR;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
