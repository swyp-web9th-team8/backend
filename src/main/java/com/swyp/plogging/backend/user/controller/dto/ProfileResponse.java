package com.swyp.plogging.backend.user.controller.dto;

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
    private String lastBadge;

    @QueryProjection
    public ProfileResponse(Long id, String nickname, String email, String region,
        String profileImageUrl, int writtenPostsCount, String lastBadgeIconUrl) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.region = region;
        this.profileImageUrl = profileImageUrl;
        this.writtenPostsCount = writtenPostsCount;
        this.lastBadge = lastBadgeIconUrl;
    }
}
