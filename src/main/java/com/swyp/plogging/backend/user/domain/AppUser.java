package com.swyp.plogging.backend.user.domain;

import com.swyp.plogging.backend.domain.UserBadge;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import com.swyp.plogging.backend.post.domain.Participation;
import com.swyp.plogging.backend.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String region;

    private boolean pushEnabled;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private boolean registered = false;

    private String profileImageUrl;

    private int score;

    @OneToMany(mappedBy = "writer")
    private List<Post> writtenPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Participation> participations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    public static AppUser newInstance(String email, String nickname, String region, AuthProvider authProvider) {
        AppUser user = new AppUser();
        user.email = email;
        user.nickname = nickname;
        user.region = region;
        user.authProvider = authProvider;
        user.registered = false;

        return user;
    }
}
