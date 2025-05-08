package com.swyp.plogging.backend.user.controller.dto;

import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Data;

@Data
public class EditableProfileResponse {

    private Long id;
    private String nickname;
    private String phoneNum;
    private String profileImageUrl;

    public static EditableProfileResponse of(AppUser appUser) {
        EditableProfileResponse vo = new EditableProfileResponse();
        vo.setId(appUser.getId());
        vo.setNickname(appUser.getNickname());
        vo.setPhoneNum(appUser.getPhoneNum());
        vo.setProfileImageUrl(appUser.getProfileImageUrl());
        return vo;
    }
}
