package com.swyp.plogging.backend.certificate.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CertificationRequest {
    private List<Long> userIds;
}
