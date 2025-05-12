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
@Schema(description = "에러 응답")
public class ErrorResponse {
    @Schema(description = "성공 여부", example = "false")
    private boolean success;

    @Schema(description = "에러 메시지", example = "Not authenticated")
    private String error;
}