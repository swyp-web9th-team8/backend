package com.swyp.plogging.backend.controller.DTO;

import com.swyp.plogging.backend.domain.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostInfoResponse {
    private Long id;
    private String title;
    private LocalDateTime meetingTime;
    private String placeId;
    private String placeName;
    private String address;
    private int participantCount;

    public PostInfoResponse(Post post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.meetingTime = post.getMeetingDt();
        this.placeId = post.getPlaceId();
        this.placeName = post.getPlaceName();
        this.address = post.getAddress();
        this.participantCount = post.getParticipations().size();
    }
}
