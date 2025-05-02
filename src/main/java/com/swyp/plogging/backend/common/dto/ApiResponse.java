package com.swyp.plogging.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ApiResponse<T> ok(T data, String message) {
        statusCode = HttpStatus.OK;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();

        return this;
    }

    public ApiResponse<T> error(String message) {
        statusCode = HttpStatus.BAD_REQUEST;
        this.message = message;
        this.timestamp = LocalDateTime.now();

        return this;
    }
}
