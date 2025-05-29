package com.swyp.plogging.backend.notification.repository;

import com.swyp.plogging.backend.notification.domain.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
}
