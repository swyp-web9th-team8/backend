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

    private ApiResponse(HttpStatusCode statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(status, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }
}
