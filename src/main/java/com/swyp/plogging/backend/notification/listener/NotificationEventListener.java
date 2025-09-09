package com.swyp.plogging.backend.notification.listener;

import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.service.NotificationService;
import com.swyp.plogging.backend.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class NotificationEventListener {
    private final NotificationStrategy strategy;

    @EventListener
    @Async
    public void listen(NotificationEvent event){
        log.info("이벤트 발생: 모임ID-{}, 이벤트 타입-{}",event.getPostId(),event.getType());
        NotificationService service = strategy.getService(event.getStrategy());
        service.notify(event);
    }
}
