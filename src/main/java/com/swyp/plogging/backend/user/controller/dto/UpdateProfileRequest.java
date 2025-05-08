package com.swyp.plogging.backend.user.controller.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;
    private String region;
    private String profileImageUrl;
    private Boolean pushEnabled;
    private String phoneNum;
}
