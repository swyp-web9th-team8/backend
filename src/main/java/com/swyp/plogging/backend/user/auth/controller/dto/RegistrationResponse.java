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
@Schema(description = "등록 완료 응답")
public class RegistrationResponse {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메시지", example = "Registration completed")
    private String message;
}