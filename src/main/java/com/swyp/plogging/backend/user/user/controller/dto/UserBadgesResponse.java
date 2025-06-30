package com.swyp.plogging.backend.user.user.controller.dto;

import java.util.List;
import lombok.Data;

@Data
public class UserBadgesResponse {

    private int remainingActionsForNextBadge;
    private List<UserBadgeResponse> userBadge;

    public UserBadgesResponse(int remainingActionsForNextBadge, List<UserBadgeResponse> userBadge) {
        this.remainingActionsForNextBadge = remainingActionsForNextBadge;
        this.userBadge = userBadge;
    }
}
