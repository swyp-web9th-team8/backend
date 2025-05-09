package com.swyp.plogging.backend.post.controller.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    private Long id;
    private String title;
    private String content;
    private String placeId;
    private String placeName;
    private String address;
    private LocalDateTime meetingTime;
    private String openChatUrl;
    private int maxParticipants;
}
