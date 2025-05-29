package com.swyp.plogging.backend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.swyp.plogging.backend.notification.domain.AppNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FCMSender implements Sender{
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };

    @Override
    @Transactional
    public void send(AppNotification notification) {
        String token = notification.getUser().getFcmToken();

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getMessage())
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("-----FCM 알림 메세지 보내기 성공-----\n{}:{}",notification.getTitle(), notification.getMessage());
            notification.sentNow();
        }catch (FirebaseMessagingException fe){
            log.debug("FCM 알림 보내기 실패");
            throw new RuntimeException(fe.getMessage());
        }
    }
}
