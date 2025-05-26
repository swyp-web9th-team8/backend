package com.swyp.plogging.backend.participation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyPostResponse {

    private final Long id;
    private final String title;
    private final String address;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime meetingDt;
    private final boolean completed;

    @QueryProjection
    public MyPostResponse(Long id, String title, String address, LocalDateTime meetingDt, boolean completed) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.meetingDt = meetingDt;
        this.completed = completed;
    }
}
