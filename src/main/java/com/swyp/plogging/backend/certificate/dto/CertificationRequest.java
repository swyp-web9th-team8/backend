package com.swyp.plogging.backend.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Schema(description = "모임 리뷰를 위한 인증 객체")
@Getter
public class CertificationRequest {
    @Schema(description = "모임에 참석한 사람들(user)의 id 배열", example = "[2,4,5]")
    private List<Long> userIds;
}
