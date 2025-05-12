package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class FileDeleteException extends CustomException {

    public FileDeleteException() {
        super("An error occurred while deleting the file", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public FileDeleteException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
