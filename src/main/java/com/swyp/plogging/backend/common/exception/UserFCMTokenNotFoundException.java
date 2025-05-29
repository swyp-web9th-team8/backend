package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UserFCMTokenNotFoundException extends CustomException {
    public UserFCMTokenNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
    public UserFCMTokenNotFoundException() {
        super("유저의 FCM 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
