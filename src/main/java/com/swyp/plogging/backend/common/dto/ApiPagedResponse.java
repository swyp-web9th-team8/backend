package com.swyp.plogging.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private ApiPagedResponse(HttpStatus httpStatus, Page<T> page, String message){
        this.statusCode = httpStatus;
        this.message = message;
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiPagedResponse<T> ok(Page<T> page, String message) {
        return new ApiPagedResponse<>(HttpStatus.OK, page, message);
    }

    public static <T> ApiPagedResponse<T> error(String message) {
        return new ApiPagedResponse<>(HttpStatus.BAD_REQUEST, new PageImpl<>(new ArrayList<>()), message);
    }
}
