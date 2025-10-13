package com.princz_mia.viaual04_gourmetgo_backend.exception;

public class AlreadyExistsException extends AppException {

    public AlreadyExistsException(String message) {
        super(message, ErrorType.RESOURCE_ALREADY_EXISTS);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, ErrorType.RESOURCE_ALREADY_EXISTS, cause);
    }
}
