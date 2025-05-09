package com.swyp.plogging.backend.post.domain;

import com.swyp.plogging.backend.controller.dto.UserResponse;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.user.domain.AppUser;
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

    public void setUpDeadLine(Integer timeFromStart) {
        if (timeFromStart == null) {
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
        response.setWriter(new UserResponse(writer));
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

    public void modify(String title,
        String content,
        LocalDateTime meetingTime,
        String placeId,
        String placeName,
        String address,
        Integer maxParticipants,
        String openChatUrl,
        Integer deadLine) {
        if (title != null && !this.title.equals(title)) {
            this.title = title;
        }

        if (content != null && !this.content.equals(content)) {
            this.content = content;
        }

        if (meetingTime != null && !this.meetingDt.equals(meetingTime)) {
            this.meetingDt = meetingTime;
        }

        if (placeId != null && !this.placeId.equals(placeId)) {
            this.placeId = placeId;
        }

        if (placeName != null && !this.placeName.equals(placeName)) {
            this.placeName = placeName;
        }

        if (address != null && !this.address.equals(address)) {
            this.address = address;
        }

        if (maxParticipants != null && maxParticipants > 0 && this.maxParticipants != maxParticipants) {
            this.maxParticipants = maxParticipants;
        }

        if (openChatUrl != null && !this.openChatUrl.equals(openChatUrl)) {
            this.openChatUrl = openChatUrl;
        }

        if (deadLine != null) {
            setUpDeadLine(deadLine);
        }
    }


    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    public Participation leave(AppUser user) {
        Participation removed = isParticipating(user);
        if(removed != null){
            participations.remove(removed);
            return removed;
        }
        return null;
    }

    public Participation isParticipating(AppUser user) {
        for(Participation participation : participations){
            if(participation.getUser().getId() != null && participation.getUser().getId().equals(user.getId())){
                return participation;
            }
            if(participation.getUser().getEmail() != null && participation.getUser().getEmail().equals(user.getEmail())){
                return participation;
            }
        }
        return null;
    }


    public boolean isMax() {
        return participations.size() == maxParticipants;
    }

    public boolean isWriter(AppUser user) {
        if(writer.getId() != null && writer.getId().equals(user.getId())){
            return true;
        }
        if (writer.getEmail() != null && writer.getEmail().equals(user.getEmail())) {
            return true;
        }
        return false;
    }
}
