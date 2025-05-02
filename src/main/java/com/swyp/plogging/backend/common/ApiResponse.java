package com.swyp.plogging.backend.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class ApiResponse<T> {

    private HttpStatusCode statusCode;
    private String message;
    private T data;

    public ApiResponse<T> ok(T data, String message) {
        statusCode = HttpStatus.OK;
        this.message = message;
        this.data = data;
        return this;
    }

    public ApiResponse<T> error(String message) {
        statusCode = HttpStatus.BAD_REQUEST;
        this.message = message;
        return this;
    }
}
