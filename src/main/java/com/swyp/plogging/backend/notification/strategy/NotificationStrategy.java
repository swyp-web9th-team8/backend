package com.swyp.plogging.backend.notification.strategy;

import com.swyp.plogging.backend.notification.service.FCMPushNotificationService;
import com.swyp.plogging.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationStrategy {
    private final FCMPushNotificationService fcmService;

    public NotificationService getService(NotiStrategy strategy){
        if(strategy.equals(NotiStrategy.FCM)){
            return fcmService;
        }
        return null;
    }
}
