package com.swyp.plogging.backend.domain;

import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String message;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private String targetRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}
