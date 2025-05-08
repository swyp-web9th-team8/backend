package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class FileUploadException extends CustomException {

    public FileUploadException() {
        super("An error occurred while uploading the file", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public FileUploadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
