package com.swyp.plogging.backend.user.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoUpdateRequest {

    String nickname;
    String region;
    String profileImageUrl;
    Boolean pushEnabled;
}
