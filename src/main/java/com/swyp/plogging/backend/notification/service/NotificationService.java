package com.swyp.plogging.backend.notification.service;

import com.swyp.plogging.backend.notification.domain.AppNotification;
import com.swyp.plogging.backend.notification.event.NotificationEvent;

public abstract class NotificationService {
    protected Sender sender;

    protected NotificationService(Sender sender){
        this.sender = sender;
    }

    public abstract AppNotification newNotification(NotificationEvent event);

    public abstract void notify(NotificationEvent event);
    public abstract void notify(AppNotification appNotification);
}
