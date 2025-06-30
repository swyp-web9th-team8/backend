package com.swyp.plogging.backend.user.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 정보 요청")
public class SignupRequest {

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    @Size(max = 10, message = "닉네임은 최대 10자까지만 입력 가능합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영어, 숫자만 입력 가능합니다.")
    private String nickname;

    @Schema(description = "사용자 성별", example = "MALE", allowableValues = {"MALE", "FEMALE"})
    @NotBlank(message = "성별은 필수 선택 사항입니다.")
    private String gender;

    @Schema(description = "거주 지역", example = "서울시 강남구")
    @NotBlank(message = "거주 지역은 필수 선택 사항입니다.")
    private String region;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
}