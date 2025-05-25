package com.swyp.plogging.backend.badge.event;

import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.Getter;

@Getter
public class CompletePostEvent {
    private final AppUser appUser;

    public CompletePostEvent(AppUser appUser) {
        this.appUser = appUser;
    }
}
