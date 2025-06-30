package com.swyp.plogging.backend.user.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.plogging.backend.user.user.domain.UserBadge;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserBadgeResponse {

    private String badgeName;
    private int remainingActionsForNextBadge;
    private String grantedReason;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime grantedAt;
    private String activeBadgeIconDir;
    private String inactiveBadgeIconDir;

    public static UserBadgeResponse from(UserBadge userBadge) {
        UserBadgeResponse vo = new UserBadgeResponse();
        vo.setBadgeName(userBadge.getBadge().getName());
        vo.setGrantedReason(userBadge.getGrantedReason());
        vo.setGrantedAt(userBadge.getCreatedDt());
        vo.setActiveBadgeIconDir(userBadge.getBadge().getActiveBadgeIconDir());
        vo.setInactiveBadgeIconDir(userBadge.getBadge().getInactiveBadgeIconDir());
        return vo;
    }
}
