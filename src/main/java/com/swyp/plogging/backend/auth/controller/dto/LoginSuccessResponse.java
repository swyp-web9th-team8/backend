package com.swyp.plogging.backend.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 성공 응답")
public class LoginSuccessResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 정보")
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "닉네임", example = "홍길동")
        private String nickname;

        @Schema(description = "지역", example = "서울시 강남구")
        private String region;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImageUrl;

        @Schema(description = "회원가입 완료 여부", example = "true")
        private boolean registered;

        @Schema(description = "인증 제공자", example = "KAKAO")
        private String provider;
    }
}












