package com.swyp.plogging.backend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.swyp.plogging.backend.notification.domain.AppNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            notification.sentNow();
        }catch (FirebaseMessagingException fe){
            throw new RuntimeException(fe.getMessage());
        }
    }
}
