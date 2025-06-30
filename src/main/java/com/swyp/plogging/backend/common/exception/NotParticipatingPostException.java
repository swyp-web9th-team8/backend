package com.swyp.plogging.backend.common.exception;

import com.swyp.plogging.backend.user.user.domain.AppUser;
import org.springframework.http.HttpStatus;

public class NotParticipatingPostException extends CustomException{

    private static final String writer = "작성자 입니다.";
    private static final String already = " 님은 이미 참가중입니다.";
    private static final String cantNot = "해당 모임에 참석할 수 없습니다.";

    protected NotParticipatingPostException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public NotParticipatingPostException(){
        this(cantNot,HttpStatus.BAD_REQUEST);
    }
    public NotParticipatingPostException(AppUser user){
        this(user.getNickname() + already, HttpStatus.BAD_REQUEST);
    }

    public static NotParticipatingPostException isWriter(){
        return new NotParticipatingPostException(writer, HttpStatus.BAD_REQUEST);
    }
}
