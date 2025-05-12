package com.swyp.plogging.backend.common.exception;

import org.springframework.http.HttpStatus;

public class PostNotFoundException extends CustomException{
    public PostNotFoundException(){
        super("해당 모임을 찾을 수 없습니다.",HttpStatus.NOT_FOUND);
    }
    public PostNotFoundException(String message){
        super(message, HttpStatus.NOT_FOUND);
    }

}
