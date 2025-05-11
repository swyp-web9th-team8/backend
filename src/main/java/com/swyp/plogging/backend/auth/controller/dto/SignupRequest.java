package com.swyp.plogging.backend.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    @Size(max = 10, message = "닉네임은 최대 10자까지만 입력 가능합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영어, 숫자만 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "성별은 필수 선택 사항입니다.")
    private String gender; // 남성 또는 여성

    @NotBlank(message = "거주 지역은 필수 선택 사항입니다.")
    private String region;

    private String profileImageUrl; // 선택 사항
}