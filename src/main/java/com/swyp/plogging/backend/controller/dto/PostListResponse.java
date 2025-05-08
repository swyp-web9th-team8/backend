package com.swyp.plogging.backend.controller.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;


// todo ApiPaged... 생겨서 제거
@Getter
public class PostListResponse<T> {

    private List<T> content = new ArrayList<>();
    private long totalPage;
    private int pageSize;
    private long totalElements;
    private int number;

    private PostListResponse(List<T> content, int totalPages, int pageSize, Long totalElements, int number) {
        this.content = content;
        this.totalPage = totalPages;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.number = number;
    }


    public static <T> PostListResponse<T> of(Page<T> response) {
        return new PostListResponse<>(
            response.getContent(),
            response.getTotalPages(),
            response.getSize(),
            response.getTotalElements(),
            response.getNumber());
    }

    public static <T> PostListResponse<T> of(List<T> content, Pageable pageable, long totalElements) {
        return PostListResponse.of(new PageImpl<T>(content, pageable, totalElements));
    }
}
