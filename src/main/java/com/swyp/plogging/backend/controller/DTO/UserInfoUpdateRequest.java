package com.swyp.plogging.backend.controller.DTO;

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
