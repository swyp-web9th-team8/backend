package com.swyp.plogging.backend.controller.dto;

import com.swyp.plogging.backend.domain.AppUser;
import lombok.Getter;
import lombok.Setter;

/**
 * { "id": 12345, "email": "user@gmail.com", "name": "홍길동", "profileImage": "https://lh3.googleusercontent.com/a/...", "provider": "google",
 * "createdAt": "2023-01-01T00:00:00Z" }
 */
@Getter
@Setter
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String profileImage;
    private String provider;
    private String createdAt;

    public UserResponse(AppUser writer) {
        this.id = writer.getId();
        this.email = writer.getEmail();
        this.name = writer.getNickname();
        this.profileImage = writer.getProfileImageUrl();
        this.provider = writer.getAuthProvider().toString();
        this.createdAt = writer.getCreatedDt().toString();
    }
}
