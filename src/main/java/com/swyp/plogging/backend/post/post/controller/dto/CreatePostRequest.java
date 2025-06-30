package com.swyp.plogging.backend.post.post.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "모임을 생성 또는 수정하기 위한 요청 객체")
@Getter
@Setter
public class CreatePostRequest {

    @Schema(description = "수정할 때만 필요")
    private Long id;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "모임장 한마디")
    private String content;
    @Schema(description = "미정")
    private String placeId;
    @Schema(description = "장소의 별칭")
    private String placeName;
    @Schema(description = "도로명 주소")
    private String address;
    @Schema(description = "모임시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime meetingTime;
    @Schema(description = "오픈채팅방 주소")
    private String openChatUrl;
    @Schema(description = "최대 참석인원(모임장 미포함)")
    private int maxParticipants;
    @Schema(description = "마감시간 = 시작전 30 or 60")
    private int deadline;

    // 위치 정보 추가
    @Schema(description = "위도")
    private Double latitude;
    @Schema(description = "경도")
    private Double longitude;
}
