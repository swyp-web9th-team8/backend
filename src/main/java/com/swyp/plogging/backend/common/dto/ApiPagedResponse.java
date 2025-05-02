package com.swyp.plogging.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
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

    public ApiPagedResponse<T> ok(Page<T> page, String message) {
        this.statusCode = HttpStatus.OK;
        this.message = message;
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.timestamp = LocalDateTime.now();

        return this;
    }

    public ApiPagedResponse<T> error(String message) {
        this.statusCode = HttpStatus.BAD_REQUEST;
        this.message = message;
        this.timestamp = LocalDateTime.now();

        return this;
    }
}
