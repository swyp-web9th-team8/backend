package com.swyp.plogging.backend.participation.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyPostResponse {

    private final Long id;
    private final String title;
    private final String placeName;
    private final LocalDateTime meetingDt;
    private final boolean completed;

    @QueryProjection
    public MyPostResponse(Long id, String title, String placeName, LocalDateTime meetingDt, boolean completed) {
        this.id = id;
        this.title = title;
        this.placeName = placeName;
        this.meetingDt = meetingDt;
        this.completed = completed;
    }
}
