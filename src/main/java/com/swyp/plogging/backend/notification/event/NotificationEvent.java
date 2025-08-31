package com.swyp.plogging.backend.notification.event;

import com.swyp.plogging.backend.notification.strategy.NotiStrategy;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private NotiStrategy strategy;
    private NotiType type;
    private AppUser receiver;
    private Long postId;
}
