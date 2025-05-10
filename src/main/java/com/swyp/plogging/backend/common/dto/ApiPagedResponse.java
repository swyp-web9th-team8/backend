package com.swyp.plogging.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class ApiPagedResponse<T> {

    private HttpStatusCode statusCode;
    private String message;
    private List<T> content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int page;
    private int size;
    private int totalPages;
    private long totalElements;

    private ApiPagedResponse(HttpStatusCode statusCode, String message, List<T> content, int page, int size, int totalPages,
        long totalElements) {
        this.statusCode = statusCode;
        this.message = message;
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiPagedResponse<T> ok(Page<T> page, String message) {
        return new ApiPagedResponse<>(
            HttpStatus.OK,
            message,
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }

    public static <T> ApiPagedResponse<T> error(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    public static <T> ApiPagedResponse<T> error(String message, HttpStatus status) {
        return new ApiPagedResponse<>(status, message, Collections.emptyList(), 0, 0, 0, 0);
    }
}
