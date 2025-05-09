package com.swyp.plogging.backend.post.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.swyp.plogging.backend.controller.dto.UserResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private NicknameAndImageResponse writer;
    private String placeId;
    private String placeName;
    private String address;
    private LocalDateTime meetingTime;
    private String openChatUrl;
    private int maxParticipants;
    private LocalDateTime deadLine;
    private List<NicknameAndImageResponse> participants;
}
