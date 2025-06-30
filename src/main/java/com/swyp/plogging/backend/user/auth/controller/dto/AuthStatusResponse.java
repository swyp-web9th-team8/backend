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
@Schema(description = "인증 상태 응답")
public class AuthStatusResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "인증 여부", example = "true")
    private boolean authenticated;
}
