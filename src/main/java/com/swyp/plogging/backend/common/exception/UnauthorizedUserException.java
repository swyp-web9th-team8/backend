package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedUserException extends CustomException {

    public UnauthorizedUserException() {
        super("Accessible to authenticated users only.", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedUserException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
