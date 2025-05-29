package com.swyp.plogging.backend.notification.service;

import com.swyp.plogging.backend.notification.domain.AppNotification;

public interface Sender {
    void send(AppNotification appNotification);
}
