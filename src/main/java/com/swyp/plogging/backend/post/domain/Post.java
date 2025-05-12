package com.swyp.plogging.backend.post.domain;

import com.swyp.plogging.backend.certificate.domain.Certification;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.post.controller.dto.NicknameAndImageResponse;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // 위치 정보 추가
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // PostGIS 공간 데이터 타입 추가
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

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

    // 위치 정보를 포함한 PostDetailResponse 변환 메서드
    public PostDetailResponse toDetailResponse() {
        PostDetailResponse response = new PostDetailResponse();
        response.setId(id);
        response.setTitle(title);
        response.setContent(content);
        response.setPlaceId(placeId);
        response.setPlaceName(placeName);
        response.setAddress(address);
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setMaxParticipants(maxParticipants);
        response.setWriter(new NicknameAndImageResponse(writer));
        response.setMeetingTime(meetingDt);
        response.setDeadLine(deadLine);
        response.setOpenChatUrl(openChatUrl);
        response.setParticipants(
                participations.stream()
                        .map(participation -> new NicknameAndImageResponse(participation.getUser()))
                        .collect(Collectors.toList())
        );
        return response;
    }

    // 위치 정보를 포함한 modify 메서드
    public void modify(String title,
                       String content,
                       LocalDateTime meetingTime,
                       String placeId,
                       String placeName,
                       String address,
                       Double latitude,
                       Double longitude,
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

        // 위치 정보 업데이트
        if (latitude != null) {
            this.latitude = latitude;
        }

        if (longitude != null) {
            this.longitude = longitude;
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

        // Point 객체 업데이트 (latitude와 longitude가 모두 있는 경우)
        if (this.latitude != null && this.longitude != null) {
            this.location = createPoint(this.longitude, this.latitude);
        }
    }

    // Point 객체 생성 유틸리티 메서드
    private Point createPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        try {
            // WKT(Well-Known Text) 형식으로 Point 생성
            String pointWKT = String.format("POINT(%f %f)", longitude, latitude);
            org.locationtech.jts.io.WKTReader wktReader = new org.locationtech.jts.io.WKTReader();
            Point point = (Point) wktReader.read(pointWKT);
            point.setSRID(4326); // WGS84 좌표계
            return point;
        } catch (Exception e) {
            return null;
        }
    }

    // 나머지 메서드는 그대로 유지
    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    public Participation leave(AppUser user) {
        Participation removed = isParticipating(user);
        if (removed != null) {
            participations.remove(removed);
            return removed;
        }
        return null;
    }

    public Participation isParticipating(AppUser user) {
        for (Participation participation : participations) {
            if (participation.getUser().getId() != null && participation.getUser().getId().equals(user.getId())) {
                return participation;
            }
            if (participation.getUser().getEmail() != null && participation.getUser().getEmail().equals(user.getEmail())) {
                return participation;
            }
        }
        return null;
    }

    public boolean isMax() {
        return participations.size() == maxParticipants;
    }

    public boolean isWriter(AppUser user) {
        if (writer.getId() != null && writer.getId().equals(user.getId())) {
            return true;
        }
        if (writer.getEmail() != null && writer.getEmail().equals(user.getEmail())) {
            return true;
        }
        return false;
    }

    public void complete() {
        completed = true;
    }
}