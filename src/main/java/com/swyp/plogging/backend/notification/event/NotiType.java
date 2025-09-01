package com.swyp.plogging.backend.notification.event;

import lombok.Getter;

@Getter
public enum NotiType {
    CREATE("생성"), REVIEW("리뷰 알림"), TEST("테스트 알림");

    private final String message;

    NotiType(String message) {
        this.message = message;
    }
}
