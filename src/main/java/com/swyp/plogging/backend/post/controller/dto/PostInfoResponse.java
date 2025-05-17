package com.swyp.plogging.backend.post.controller.dto;

import com.swyp.plogging.backend.certificate.domain.Certification;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<NicknameAndImageResponse> participants;
    private String thumbnail;
    private boolean iIn;

    // 위치 정보 추가
    private Double latitude;
    private Double longitude;

    public PostInfoResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.meetingTime = post.getMeetingDt();
        this.placeId = post.getPlaceId();
        this.placeName = post.getPlaceName();
        this.address = post.getAddress();
        this.participantCount = post.getParticipations().size();
        this.participants = post.getParticipations().stream()
                .map(
                        participation -> new NicknameAndImageResponse(participation.getUser())
                )
                .collect(Collectors.toList());
        this.latitude = post.getLatitude();
        this.longitude = post.getLongitude();
        this.thumbnail = null;
    }

    public PostInfoResponse(Post post, Certification certification) {
        this(post);
        // 완료된 모임에 첫번째 인증 이미지 추가
        List<String> images = certification.getImageUrls();
        if (post.isCompleted() && !images.isEmpty()) {
            this.thumbnail = images.get(0);
        } else {
            this.thumbnail = null;
        }
    }

    public PostInfoResponse(Post post, AppUser user){
        this(post);
        if(post.isWriter(user)){
            this.iIn = true;
        }
        if(!post.getParticipations().isEmpty() && post.getParticipations().stream().anyMatch(participation ->
                participation.getUser().getId().equals(user.getId()))){
            this.iIn = true;
        };
    }
}