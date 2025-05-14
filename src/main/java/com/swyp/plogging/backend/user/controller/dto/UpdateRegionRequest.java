package com.swyp.plogging.backend.user.controller.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UpdateRegionRequest implements UpdateProfileRequest {
    // 기존 문자열 형태의 지역
    private String region;

    // Region ID를 직접 사용할 경우 (선택적)
    private Long regionId;
}