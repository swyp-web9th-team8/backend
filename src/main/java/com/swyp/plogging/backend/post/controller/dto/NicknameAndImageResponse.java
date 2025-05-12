package com.swyp.plogging.backend.post.controller.dto;

import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Getter;
import lombok.Setter;

/**
 * {"nickname": "홍길동", "profileImage": "https://lh3.googleusercontent.com/a/..."}
 */
@Getter
@Setter
public class NicknameAndImageResponse {
    private Long id;
    private String nickname;
    private String profileImage;

    public NicknameAndImageResponse(AppUser writer) {
        this.id = writer.getId();
        this.nickname = writer.getNickname();
        this.profileImage = writer.getProfileImageUrl();
    }
}
