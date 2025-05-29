package com.swyp.plogging.backend.notification.listener;

import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.service.NotificationService;
import com.swyp.plogging.backend.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NotificationEventListener {
    private final NotificationStrategy strategy;

    @EventListener
    public void listen(NotificationEvent event){
        NotificationService service = strategy.getService(event.getStrategy());
        service.notify(event);
    }
}
