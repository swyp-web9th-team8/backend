package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class CertificationException extends CustomException {

    private static final String notPart = "참가하지 않은 모임입니다.";
    private static final String minImage = "최소 1개 이상의 이미지가 필요합니다.";

    public CertificationException() {
        super("Fail to certificate post", HttpStatus.UNAUTHORIZED);
    }

    public CertificationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public CertificationException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public static CertificationException notParticipated() {
        return new CertificationException(notPart, HttpStatus.BAD_REQUEST);
    }
    public static CertificationException needMinOneImage() {
        return new CertificationException(minImage, HttpStatus.BAD_REQUEST);
    }
}
