package com.swyp.plogging.backend.user.user.controller.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UpdatePhoneNumRequest implements UpdateProfileRequest {

    private String phoneNum;
}
