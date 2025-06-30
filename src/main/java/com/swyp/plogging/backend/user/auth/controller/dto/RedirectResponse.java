package com.swyp.plogging.backend.user.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리다이렉트 응답")
public class RedirectResponse {
    @Schema(description = "리다이렉트 URL", example = "https://kauth.kakao.com/oauth/authorize?client_id=...")
    private String redirectUrl;
}
