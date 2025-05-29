package com.swyp.plogging.backend.notification.event;

import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationEvent {
    private NotiStrategy strategy;
    private NotiType type;
    private AppUser user;
    private Long postId;
}
