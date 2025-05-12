package com.swyp.plogging.backend.post.controller.dto;

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
    private NicknameAndImageResponse writer;
    private String placeId;
    private String placeName;
    private String address;
    private LocalDateTime meetingTime;
    private String openChatUrl;
    private int maxParticipants;
    private LocalDateTime deadLine;
    private List<NicknameAndImageResponse> participants;
    private List<String> imageUrls;

    // 위치 정보 추가
    private Double latitude;
    private Double longitude;
}
