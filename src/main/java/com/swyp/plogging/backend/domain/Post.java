package com.swyp.plogging.backend.domain;

import com.swyp.plogging.backend.controller.DTO.PostDetailResponse;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String content;

    private String placeId;
    private String placeName;
    private String address;

    private LocalDateTime meetingDt;
    private LocalDateTime deadLine;
    private String openChatUrl;
    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private AppUser writer;

    @Builder.Default
    @OneToMany(mappedBy = "post")
    private List<Participation> participations = new ArrayList<>();
    private int maxParticipants;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Certification certification;

    public void createDeadLine(Integer timeFromStart) {
        if(timeFromStart == null){
            timeFromStart = 30;
        }
        deadLine = meetingDt.minusMinutes(timeFromStart);
    }

    public PostDetailResponse toDetailResponse() {
        PostDetailResponse response = new PostDetailResponse();
        response.setId(id);
        response.setTitle(title);
        response.setContent(content);
        response.setPlaceId(placeId);
        response.setPlaceName(placeName);
        response.setAddress(address);
        response.setMaxParticipants(maxParticipants);
        // todo 로그인 구현 후 수정 필요
        //        response.setWriter(new UserResponse(writer));
        response.setMeetingTime(meetingDt);
        response.setDeadLine(deadLine);
        response.setOpenChatUrl(openChatUrl);
        //        response.setParticipants(
//                participations.stream()
//                        .map(participation -> new UserResponse(participation.getUser()))
//                        .collect(Collectors.toList())
//        );
        return response;
    }
}
