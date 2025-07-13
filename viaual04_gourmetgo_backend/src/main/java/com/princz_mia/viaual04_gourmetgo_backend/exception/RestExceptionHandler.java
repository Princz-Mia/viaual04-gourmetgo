package com.princz_mia.viaual04_gourmetgo_backend.exception;

import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = { AppException.class })
    @ResponseBody
    public ResponseEntity<ErrorDto> handleException(AppException exception) {
        return ResponseEntity
                .status(exception.getHttpStatus())
                .body(ErrorDto.builder().message(exception.getMessage()).build());
    }

    @ExceptionHandler(LockedException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> handleLocked(LockedException ex) {
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(new ApiResponse("Your account is locked. Please contact support.", null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> handleBadCreds(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Invalid email or password.", null));
    }
}
