package com.swyp.plogging.backend.notification.service;

import com.swyp.plogging.backend.notification.domain.AppNotification;
import com.swyp.plogging.backend.notification.event.NotificationEvent;
import com.swyp.plogging.backend.notification.repository.AppNotificationRepository;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FCMPushNotificationService extends NotificationService{
    private final AppNotificationRepository repository;
    private final PostService postService;

    FCMPushNotificationService(Sender FCMSender, AppNotificationRepository appNotificationRepository, PostService postService){
        super(FCMSender);
        this.repository = appNotificationRepository;
        this.postService = postService;
    }

    @Transactional
    public AppNotification newNotification(NotificationEvent event){
        Post post = postService.findById(event.getPostId());
        AppNotification noti = AppNotification.newInstance(event.getType(),event.getUser(), post);
        return repository.save(noti);
    }

    public void notify(NotificationEvent event){
        notify(newNotification(event));
    }

    @Override
    public void notify(AppNotification appNotification) {
        sender.send(appNotification);
    }
}
