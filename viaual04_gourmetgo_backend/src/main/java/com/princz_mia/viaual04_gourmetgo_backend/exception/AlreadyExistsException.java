package com.princz_mia.viaual04_gourmetgo_backend.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends AppException {

    public AlreadyExistsException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
