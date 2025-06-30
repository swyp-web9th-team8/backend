package com.swyp.plogging.backend.user.user.controller.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class ProfileResponse {

    private Long id;
    private String nickname;
    private String email;
    private String region;
    private String profileImageUrl;
    private int writtenPostsCount;
    private String lastBadgeIconDir;
    private int participatedCount;
    private int totalMeet;

    @QueryProjection
    public ProfileResponse(Long id, String nickname, String email, String region,
        String profileImageUrl, int writtenPostsCount, String inactiveBadgeIconDir, int participatedCount) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.region = region;
        this.profileImageUrl = profileImageUrl;
        this.lastBadgeIconDir = inactiveBadgeIconDir;
        this.writtenPostsCount = writtenPostsCount;
        this.participatedCount = participatedCount;
        this.totalMeet = calculateTotalMeet(writtenPostsCount, participatedCount);
    }

    public int calculateTotalMeet(int writtenPostsCount, int participatedCount) {
        return writtenPostsCount + participatedCount;
    }
}
