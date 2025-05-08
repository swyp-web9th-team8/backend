package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class PostNotFoundException extends CustomException{
    protected PostNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public PostNotFoundException(){
        this("해당 모임을 찾을 수 없습니다.",HttpStatus.NOT_FOUND);
    }

}
