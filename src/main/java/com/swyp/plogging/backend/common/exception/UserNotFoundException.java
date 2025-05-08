package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super("User not found with the given ID", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
