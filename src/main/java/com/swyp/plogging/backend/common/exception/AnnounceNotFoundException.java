package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class AnnounceNotFoundException extends CustomException {

    public AnnounceNotFoundException() {
        super("Announce not found with the given ID", HttpStatus.NOT_FOUND);
    }

    public AnnounceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
