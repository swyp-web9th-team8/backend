package com.swyp.plogging.backend.post.post.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime meetingTime;
    private String openChatUrl;
    private int maxParticipants;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadLine;
    private List<NicknameAndImageResponse> participants;
    private List<String> imageUrls;

    // 위치 정보 추가
    private Double latitude;
    private Double longitude;

    private boolean iIn;
}
