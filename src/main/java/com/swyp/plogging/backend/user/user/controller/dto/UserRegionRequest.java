package com.swyp.plogging.backend.user.user.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegionRequest {
    private Long regionId;
    private boolean isPrimary;
}