package com.swyp.plogging.backend.user.controller.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UpdateRegionRequest implements UpdateProfileRequest {

    private String region;
}
