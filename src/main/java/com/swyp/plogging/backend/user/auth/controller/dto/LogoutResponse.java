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
@Schema(description = "로그아웃 응답")
public class LogoutResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메시지", example = "Logged out successfully")
    private String message;
}
