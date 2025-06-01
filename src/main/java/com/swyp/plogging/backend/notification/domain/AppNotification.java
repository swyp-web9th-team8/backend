package com.swyp.plogging.backend.notification.domain;

import com.swyp.plogging.backend.notification.event.NotiType;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppNotification {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(length = 500)
    private String url;

    @Column(nullable = false)
    private boolean read;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private String targetRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static AppNotification newInstance(NotiType type, AppUser user, Post post) {
        AppNotification instance = new AppNotification();
        instance.title = "Ploggo " + type.getMessage();
        instance.user = user;
        instance.post = post;
        instance.message = String.format("%s: %s", type.getMessage(), post.getTitle());
        // 임시로 빈 문자열 생성
        instance.targetRegion = "";
        return instance;
    }

    /**
     * 테스트를 위한 새로운 인스턴스 생성
     * @param title 알림 제목
     * @param body 알림 내용
     * @param user 보낼 유저
     * @return 새로운 AppNotification 인스턴스
     */
    public static AppNotification newInstance(String title, String body, AppUser user) {
        AppNotification instance = new AppNotification();
        instance.title = title.isBlank() ? "Ploggo 알림" : title;
        instance.user = user;
        instance.post = null;
        instance.message = body;
        // 임시로 빈 문자열 생성
        instance.targetRegion = "";
        return instance;
    }

    public void sentNow() {
        this.sentAt = LocalDateTime.now();
    }
}
