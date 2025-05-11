package com.swyp.plogging.backend.rank.controller.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class RankingResponse {

    private Long userId;
    private String nickname;
    private String profileImageDir;
    private int totalMeet;
    private int rank;

    @QueryProjection
    public RankingResponse(Long userId, String nickname, String profileImageDir, Long writtenPostCount, Long participatedPostCount) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageDir = profileImageDir;
        this.totalMeet = Math.toIntExact(writtenPostCount + participatedPostCount);
    }
}
