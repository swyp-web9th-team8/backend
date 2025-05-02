package com.swyp.plogging.backend.user.controller.dto;

import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {

    private Long id;
    private String email;
    private String nickname;
    private String region;
    private boolean pushEnabled;
    private String profileImageUrl;
    private int score;
    private String authProvider;
    private boolean registered;

    public static ProfileResponse of(AppUser user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setRegion(user.getRegion());
        response.setPushEnabled(user.isPushEnabled());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setScore(user.getScore());
        response.setAuthProvider(user.getAuthProvider().name());
        response.setRegistered(user.isRegistered());
        return response;
    }
}
