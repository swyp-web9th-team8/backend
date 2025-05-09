package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedUpdateRequestException extends CustomException {

    public UnsupportedUpdateRequestException() {
        super("Unsupported update request type.", HttpStatus.BAD_REQUEST);
    }
}
