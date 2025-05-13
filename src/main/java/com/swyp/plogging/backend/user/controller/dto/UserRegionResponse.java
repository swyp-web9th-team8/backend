package com.swyp.plogging.backend.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegionResponse {
    private Long id;
    private Long regionId;
    private String city;
    private String district;
    private String neighborhood;
    private boolean isPrimary;
}