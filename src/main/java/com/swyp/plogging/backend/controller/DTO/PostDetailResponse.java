package com.swyp.plogging.backend.controller.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private UserResponse writer;
    private String placeId;
    private String placeName;
    private String address;
    private LocalDateTime meetingTime;
    private String openChatUrl;
    private int maxParticipants;
    private LocalDateTime deadLine;
    private List<UserResponse> participants;
}
